package mem

import chisel3._
import chisel3.util._
import rv64_3stage._
import device._

case class CacheConfig(
    readOnly: Boolean = false,
    hasMMIO: Boolean = true,
    name: String = "cache", // used for debug info    
    userBits: Int = 0,
    idBits: Int = 0,
    ways: Int = 4, // set associativity
    lines: Int = 4, // number of `xlen`-bit blocks in each cache line
    totalSize: Int = 32, // K Bytes
    replacementPolicy: String = "lru" // random or lru
)

trait CacheParameters extends phvntomParams {
  implicit val cacheConfig: CacheConfig

  val readOnly = cacheConfig.readOnly
  val hasMMIO = cacheConfig.hasMMIO
  val cacheName = cacheConfig.name // used for debug info
  val userBits = cacheConfig.userBits
  val idBits = cacheConfig.idBits
  val nWays = cacheConfig.ways
  val nLine = cacheConfig.lines
  val nBytes = cacheConfig.totalSize * 1024
  val nBits = nBytes * 8
  val lineBits = nLine * xlen
  val lineBytes = lineBits / 8
  val lineLength = log2Ceil(nLine)
  val nSets = nBytes / lineBytes / nWays
  val offsetLength = log2Ceil(lineBytes)
  val indexLength = log2Ceil(nSets)
  val tagLength = xlen - (indexLength + offsetLength)
  val replacementPolicy: String = cacheConfig.replacementPolicy
  val policy: ReplacementPolicyBase = if (replacementPolicy == "lru") {
    LRUPolicy
  } else { RandomPolicy }
}

class CacheSimpleIO(implicit val cacheConfig: CacheConfig)
    extends Bundle
    with CacheParameters {
  val in = new MemIO
  val mem = Flipped(new MemIO(lineBits))
  val mmio = if (hasMMIO) { Flipped(new MemIO) }
  else { null }
}

class MetaData(implicit val cacheConfig: CacheConfig) extends Bundle with CacheParameters {
  val valid = Bool()
  val dirty = if (!readOnly) { Bool() }
  else { null }
  val meta = if (replacementPolicy == "lru") { UInt(log2Ceil(nWays).W) }
  else { UInt(1.W) }
  val tag = UInt(tagLength.W)
  override def toPrintable: Printable =
    p"MetaData(valid = ${valid}, dirty = ${dirty}, meta = ${meta}, tag = 0x${Hexadecimal(tag)})\n"
}

class CacheLineData(implicit val cacheConfig: CacheConfig) extends Bundle with CacheParameters {
  val data = Vec(nLine, UInt(xlen.W))
  override def toPrintable: Printable =
    p"DCacheLineData(data = ${data})\n"
}