package tile

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.internal.instantiableMacro

class Tile extends Module {
    val io = IO ( new Bundle {
        val pc = Output(UInt(32.W))
        val instr = Output(UInt(32.W))
    })

    
    val pc = RegInit(0.U(32.W))
    pc := pc + 1.U
    io.pc := pc

    val rom = Module( new ROM_code() )
    rom.io.CS := 1.U
    rom.io.addr := pc
    val instr = rom.io.out
    io.instr := instr

    
}
