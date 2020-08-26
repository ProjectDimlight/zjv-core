package rv32_1stage
import rv32_1stage.constants._

import chisel3._
import chisel3.util._
import scala.math._

//TODO: When compiler bug SI-5604 is fixed in 2.10, change object Constants to
//      package object rocket and remove import Constants._'s from other files
object Constants extends
   SodorProcConstants with
   ScalarOpConstants with
   Common.constants.RISCVConstants with
   Common.MemoryOpConstants
{
}
