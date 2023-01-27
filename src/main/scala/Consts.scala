
package constants


import chisel3._
import chisel3.util._

trait ScalarOpConstants {
  def X = BitPat("b?")
  def N = BitPat("b0")
  def Y = BitPat("b1")

  def IMM_X  = BitPat("b???")
  def IMM_I  = BitPat("b100")
/*  def IMM_S  = 0.U(3.W)
  def IMM_SB = 1.U(3.W)
  def IMM_U  = 2.U(3.W)
  def IMM_UJ = 3.U(3.W)
  def IMM_I  = 4.U(3.W)
  def IMM_Z  = 5.U(3.W)  */
}