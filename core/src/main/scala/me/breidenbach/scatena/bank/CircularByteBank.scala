package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.DataConstants
import me.breidenbach.scatena.util.DataConstants._
import org.slf4j.LoggerFactory

/**
  * @author Kevin Breidenbach
  *         Date: 10/7/16
  */
@throws(classOf[ByteBankException])
class CircularByteBank(bufferSize: Int = CircularByteBank.defaultMemorySize) extends BufferBackedByteBank(bufferSize) {
  import CircularByteBank._

  if (bufferSize < minimumSize) {
    val message = s"buffer size must be larger than $minimumSize"
    logger.error(message)
    throw ByteBankException(message)
  }

  private var nextOffset = 0
  private var offsetAtZero = 0
  private var minimumOffset = 0
  private var minimumOffsetPosition = 0

  override def reset(): Unit = {
    offsetAtZero = 0
    nextOffset = 0
    memoryBuffer.clear()
  }

  override def add(buffer: ByteBuffer): Long = {
    if (buffer.remaining() > bufferSize) -1 else {
      val offset = calculateOffsetAndSetBufferPosition(buffer.remaining() + shortSize)
      setSizeBuffer(buffer.remaining().asInstanceOf[Short])
      memoryBuffer.put(sizeBuffer).put(buffer)
      offset
    }
  }

  override def get(offset: Long): ByteBuffer = {
    if (offset > nextOffset) emptyBuffer()
    else {
      if (offset < minimumOffset || offset >= nextOffset) emptyBuffer()
      else if (offset < offsetAtZero) getFromMemory(minimumOffsetPosition)
      else getFromMemory(offset - offsetAtZero)
    }
  }

  override def size(): Long = nextOffset

  override def flush(): Unit = {}

  override def firstOffset(): Long = offsetAtZero

  private def calculateOffsetAndSetBufferPosition(size: Int): Int = {
    val pos = memoryBuffer.position()
    val offset = nextOffset
    nextOffset += size

    if (pos + size > bufferSize) {
      offsetAtZero = offset
      memoryBuffer.clear()
    }

    findMinimumOffsetAndPosition(size)

    offset
  }

  private def findMinimumOffsetAndPosition(size: Int): Unit = {
    val currentPosition = memoryBuffer.position()
    val nextPos = currentPosition + size

    memoryBuffer.flip().limit(bufferSize)
    calculateMinimumOffsetAndPosition()
    memoryBuffer.clear().position(currentPosition)

    def calculateMinimumOffsetAndPosition(): Unit = {
      if (offsetAtZero > 0 && nextPos > minimumOffsetPosition) {
        val minimumOffsetSize = shortSize + { memoryBuffer.position(minimumOffsetPosition); memoryBuffer.getShort() }
        minimumOffsetPosition += minimumOffsetSize
        minimumOffset += minimumOffsetSize
        if (minimumOffsetPosition > bufferSize) {
          minimumOffsetPosition = 0
          minimumOffset = offsetAtZero
        }
        calculateMinimumOffsetAndPosition()
      }
    }
  }
}

object CircularByteBank {
  val minimumSize = DataConstants.udpMaxPayload * 5
  private[CircularByteBank] val defaultMemorySize = 1024 * 128
  private[CircularByteBank] val logger = LoggerFactory.getLogger(this.getClass)
}
