package tile

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._
import tileparams.HasTileParams
import Instructions._
import constants._

// the general purpose register file
class RegFile(n: Int, w: Int, zero: Boolean = false )
{
    val rf = Mem( n, UInt(w.W))
    private def access(addr: UInt) = rf(~addr(log2Up(n)-1,0))
    def read( addr: UInt) = {
        Mux(zero.B && addr === 0.U, 0.U, access(addr))
    }
    def write( addr: UInt, data: UInt ) = {
        when( addr =/= 0.U) {
            access(addr) := data
        }
    }
}

abstract trait DecodeConstants extends HasTileParams {
      val table: Array[(BitPat, List[BitPat])]
}

// This decode logic is copied directly from RocketChip, I'm not sure how it works

object DecodeLogic
{
  // TODO This should be a method on BitPat
  private def hasDontCare(bp: BitPat): Boolean = bp.mask.bitCount != bp.width
  // Pads BitPats that are safe to pad (no don't cares), errors otherwise
  private def padBP(bp: BitPat, width: Int): BitPat = {
    if (bp.width == width) bp
    else {
      require(!hasDontCare(bp), s"Cannot pad '$bp' to '$width' bits because it has don't cares")
      val diff = width - bp.width
      require(diff > 0, s"Cannot pad '$bp' to '$width' because it is already '${bp.width}' bits wide!")
      BitPat(0.U(diff.W)) ## bp
    }
  }

  def apply(addr: UInt, default: BitPat, mapping: Iterable[(BitPat, BitPat)]): UInt =
    chisel3.util.experimental.decode.decoder(QMCMinimizer, addr, TruthTable(mapping, default))
  def apply(addr: UInt, default: Seq[BitPat], mappingIn: Iterable[(BitPat, Seq[BitPat])]): Seq[UInt] = {
    val nElts = default.size
    require(mappingIn.forall(_._2.size == nElts),
      s"All Seq[BitPat] must be of the same length, got $nElts vs. ${mappingIn.find(_._2.size != nElts).get}"
    )

    val elementsGrouped = mappingIn.map(_._2).transpose
    val elementWidths = elementsGrouped.zip(default).map { case (elts, default) =>
      (default :: elts.toList).map(_.getWidth).max
    }
    val resultWidth = elementWidths.sum

    val elementIndices = elementWidths.scan(resultWidth - 1) { case (l, r) => l - r }

    // All BitPats that correspond to a given element in the result must have the same width in the
    // chisel3 decoder. We will zero pad any BitPats that are too small so long as they dont have
    // any don't cares. If there are don't cares, it is an error and the user needs to pad the
    // BitPat themselves
    val defaultsPadded = default.zip(elementWidths).map { case (bp, w) => padBP(bp, w) }
    val mappingInPadded = mappingIn.map { case (in, elts) =>
      in -> elts.zip(elementWidths).map { case (bp, w) => padBP(bp, w) }
    }
    val decoded = apply(addr, defaultsPadded.reduce(_ ## _), mappingInPadded.map { case (in, out) => (in, out.reduce(_ ## _)) })

    elementIndices.zip(elementIndices.tail).map { case (msb, lsb) => decoded(msb, lsb + 1) }.toList
  }
  //def apply(addr: UInt, default: Seq[BitPat], mappingIn: List[(UInt, Seq[BitPat])]): Seq[UInt] =
  //  apply(addr, default, mappingIn.map(m => (BitPat(m._1), m._2)).asInstanceOf[Iterable[(BitPat, Seq[BitPat])]])
  //def apply(addr: UInt, trues: Iterable[UInt], falses: Iterable[UInt]): Bool =
  //  apply(addr, BitPat.dontCare(1), trues.map(BitPat(_) -> BitPat("b1")) ++ falses.map(BitPat(_) -> BitPat("b0"))).asBool
}


class IntCtrlSigs extends Bundle with ScalarOpConstants {
    val legal = Bool()
    val sel_imm = Bits(IMM_X.getWidth.W)
    val wxd = Bool()

    def default: List[BitPat] = 
        //   legal
        //   | imm   wxd
        //   | |     |
        List(N,IMM_X,N)

    def decode( inst: UInt, table: Iterable[(BitPat, List[BitPat])]) = {
        val decoder = DecodeLogic(inst, default, table)
        val sigs = Seq( legal, sel_imm, wxd )

        sigs zip decoder map {case(s,d) => s := d}
        this
    }

}



class InstrDecode () extends DecodeConstants with ScalarOpConstants {
      val table: Array[(BitPat, List[BitPat])] = Array(
        ADDI -> List(Y,IMM_I,Y),
        LB   -> List(Y,IMM_I,N)
      )

}

class InstrDecodeOld extends Module with HasTileParams {
    val io = IO ( new Bundle{
        val instr = Input(UInt(xLen.W))
        val rd = Output(UInt(4.W))
    } )
    val opcode = Wire(UInt(7.W))
    opcode := io.instr(6,0)
    when( opcode === 3.U) { // I type - load
        io.rd := io.instr(11,7)
    } .elsewhen( opcode === 0x13.U ) {
        io.rd := io.instr(11,7)


    } .otherwise {
        io.rd := 0.U
    }
    printf("opcode = 0x%x rd=0x%x\n",opcode, io.rd)

}




