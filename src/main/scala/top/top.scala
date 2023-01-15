package top

import chisel3._
import chisel3.stage.ChiselStage

import tile.Tile

object TopLevel extends App {
    println("======== My Risc2 =================")
    println("   Top level scala!") 
    println("   Generating verilog...")
    (new ChiselStage).emitVerilog(new top, Array("--target-dir", "emulator/generated-src/"))

}

class top extends Module {
    val io = IO( new Bundle{
        val pc = Output(UInt(32.W))
        val pc_new = Output(UInt(32.W))
        val rd = Output(UInt(32.W))
        val rs1 = Output(UInt(32.W))
        val rs2 = Output(UInt(32.W))
        val op = Output(UInt(32.W))
        val op2 = Output(UInt(32.W))
        val imm = Output(UInt(32.W))
        val ecall_break = Output(UInt(32.W))
        val instr = Output(UInt(32.W))

    } )
    //printf("Hello world!\n")
    // io.pc := 0.U
    //io.instr := 0.U
    io.pc_new := 0.U
    io.rd := 0.U
    io.rs1 := 0.U
    io.rs2 := 0.U
    io.op := 0.U
    io.op2 := 0.U
    io.imm := 0.U
    io.ecall_break := 0.U

    val tile = Module( new Tile() )
    io.pc := tile.io.pc
    io.instr := tile.io.instr

}
