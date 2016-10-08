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
  private var lastOffset = 0
  private var lastOffsetInBuffer = 0
  private var lastPosition = 0
  private var lastPositionInBuffer = 0

  override def reset(): Unit = {
    offsetAtZero = 0
    nextOffset = 0
    lastOffset = 0
    lastOffsetInBuffer = 0
    lastPosition = 0
    lastPositionInBuffer = 0
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
      val usedBufferFromStart = nextOffset - offsetAtZero
      val remainingBuffer = bufferSize - usedBufferFromStart
      val minimumOffset = if (offsetAtZero - remainingBuffer < lastOffsetInBuffer) lastOffsetInBuffer else offsetAtZero
      if (offset < minimumOffset || offset >= nextOffset) emptyBuffer()
      else if (offset < offsetAtZero) getFromMemory(lastPositionInBuffer)
      else getFromMemory(offset - offsetAtZero)
    }
  }

  override def size(): Long = nextOffset

  override def flush(): Unit = {}

  private def calculateOffsetAndSetBufferPosition(size: Int): Int = {
    val offset = nextOffset
    val lastPos = memoryBuffer.position()
    nextOffset += size

    if (lastPos + size > bufferSize) {
      memoryBuffer.clear()
      offsetAtZero = offset
      lastOffsetInBuffer = lastOffset
      lastPositionInBuffer = lastPosition
    }

    lastPosition = lastPos
    lastOffset = offset
    offset
  }
}

object CircularByteBank {
  val minimumSize = DataConstants.udpMaxPayload * 5
  private[CircularByteBank] val defaultMemorySize = 1024 * 128
  private[CircularByteBank] val logger = LoggerFactory.getLogger(this.getClass)
}