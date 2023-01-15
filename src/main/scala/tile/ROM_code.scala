package tile

import chisel3._
import chisel3.stage.ChiselStage

/* from https://stackoverflow.com/questions/41947480/how-to-initialize-the-data-of-a-mem-in-chisel3

object Tools {

  def readmemh(path: String): Array[BigInt] = {
    val buffer = new ArrayBuffer[BigInt]
    for (line <- Source.fromFile(path).getLines) {
      val tokens: Array[String] = line.split("(//)").map(_.trim)
      if (tokens.length > 0 && tokens(0) != "") {
        val i = Integer.parseInt(tokens(0), 16)
        buffer.append(i)
      }
    }
    buffer.toArray
  }
}
*/

class ROM_code extends Module {
  val io = IO ( new Bundle {
    val out = Output(UInt(32.W))
    val addr = Input(UInt(32.W))
    val CS = Input(Bool())
  })

  val m = VecInit( 
    0x00000093.U, // 80000048: li   ra,0
    0x00000113.U, // 8000004c: li   sp,0
    0x00000193.U, // 80000050: li	gp,0
    0x00000213.U, // 80000054: li	tp,0
    0x00000293.U, // 80000058: li	t0,0
    ) //
  when( io.CS ) {
    io.out := m(io.addr)
  }.otherwise {
    io.out := 0.U
  }
}
