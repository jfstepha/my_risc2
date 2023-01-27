package tile

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.internal.instantiableMacro
import chisel3.util._
//import chisel3.util.MuxLookup
import tileparams.HasTileParams

// This is actually the core, I should rename it at some point

class Tile extends Module with HasTileParams {
    val io = IO ( new Bundle {
        val pc = Output(UInt(32.W))
        val instr = Output(UInt(32.W))
        val pc_new = Output(UInt(32.W))
    })

    val pc = RegInit(0.U(32.W))
    io.pc := pc
    val lgNXRegs = 4
    val regAddrMask = (1 << lgNXRegs) - 1
    val rf = new RegFile(regAddrMask, xLen)

    val rom = Module( new ROM_code() )
    rom.io.CS := 1.U
    rom.io.addr := pc
    val instr = rom.io.out
    io.instr := instr

    // the program counter mux
    val PCSel = 0.U
    val pc_branch = 0.U
    val pc_plus4 = pc + 4.U
    val pc_new = MuxLookup( PCSel, 0.U, Array(   0.U->pc_plus4, 1.U->pc_branch) )
    pc := pc_new
    io.pc_new := pc_new

    val decode_table = {
        Seq(new InstrDecode)
    } flatMap(_.table)


    val id_ctrl = Wire(new IntCtrlSigs()).decode(instr, decode_table) 
    val id_waddr = instr(11,7)
    
    val imm = ImmGen(id_ctrl.sel_imm, instr)
    printf("legal=%x wxd=%x id_waddr=0x%x\n",id_ctrl.legal, id_ctrl.wxd,id_waddr)

}

object ImmGen {
    def apply(sel: UInt, inst: UInt) {
        // only IMM_I for now
        val sign = inst(31).asSInt
        val b30_20 = sign
        val b19_12 = sign
        val b11 = sign
        val b10_5 = inst(30,25)
        val b4_1 = inst(24,21)  
        val b0 = inst(20)
        Cat(sign, b30_20, b19_12, b11, b10_5, b4_1, b0).asSInt
    }
}