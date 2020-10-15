package rv64_nstage.core

import chisel3._
import chisel3.util._
import common._
import rv64_nstage.control.ControlPath
import device.MemIO
import rv64_nstage.register.InterruptIO

trait phvntomParams {
  val xlen       = 64
  val bitWidth   = log2Ceil(xlen)
  val regNum     = 32
  val regWidth   = log2Ceil(regNum)
  val diffTest   = true
  val pipeTrace  = true
  val rtThread   = true
}


class CoreIO extends Bundle with phvntomParams {
  val imem = Flipped(new MemIO)
  val dmem = Flipped(new MemIO)
  val int = new InterruptIO
}

class Core extends Module with phvntomParams {
  val io = IO(new CoreIO)
  val dpath = Module(new DataPath)
  val cpath = Module(new ControlPath)

  dpath.io.ctrl <> cpath.io
  dpath.io.imem <> io.imem
  dpath.io.dmem <> io.dmem
  dpath.io.int  <> io.int
}