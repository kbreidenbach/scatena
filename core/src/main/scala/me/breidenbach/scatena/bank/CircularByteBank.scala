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
    if (offset < offsetAtZero || offset > nextOffset) {
      messageBuffer.clear().flip(); messageBuffer
    }
    else getFromMemory(offset, offsetAtZero)
  }

  override def size(): Long = nextOffset

  override def flush(): Unit = {}

  private def calculateOffsetAndSetBufferPosition(size: Int): Int = {
    val offset = nextOffset
    nextOffset += size

    if (memoryBuffer.position() + size > bufferSize) {
      memoryBuffer.clear()
      offsetAtZero = offset
    }
    offset
  }
}

object CircularByteBank {
  private[CircularByteBank] val defaultMemorySize = 1024 * 128
  private[CircularByteBank] val minimumSize = DataConstants.udpMaxPayload * 5
  private[CircularByteBank] def logger = LoggerFactory.getLogger(this.getClass)
}
