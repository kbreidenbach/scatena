package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.DataConstants._
import me.breidenbach.scatena.util.{BufferFactory, DataConstants}

/**
  * @author Kevin Breidenbach
  *         Date: 10/7/16
  */
@throws(classOf[ByteBankException])
class CircularByteBank(bufferSize: Int = CircularByteBank.defaultMemorySize) extends ByteBank {
  import CircularByteBank.minimumSize

  if (bufferSize < minimumSize) throw ByteBankException(s"buffer size must be larger than $minimumSize")

  private val memoryBuffer = BufferFactory.createBuffer(bufferSize)
  private val messageBuffer = BufferFactory.createBuffer()
  private val sizeBuffer = BufferFactory.createBuffer(shortSize)
  private var nextOffset = 0
  private var offsetAtZero = 0
  private val bytes = Array.ofDim[Byte](DataConstants.udpMaxPayload)

  override def reset(): Unit = {
    offsetAtZero = 0
    nextOffset = 0
    memoryBuffer.clear()
  }

  override def add(buffer: ByteBuffer): (Long) = {
    val offset = calculateOffsetAndSetBufferPosition(buffer.remaining() + shortSize)
    setSizeBuffer(buffer.remaining().asInstanceOf[Short])
    memoryBuffer.put(sizeBuffer).put(buffer)
    offset
  }

  override def add(bytes: Array[Byte]): (Long) = {
    messageBuffer.clear()
    messageBuffer.put(bytes)
    messageBuffer.flip()
    add(messageBuffer)
  }

  override def get(offset: Long): (ByteBuffer) = {
    if (offset < offsetAtZero || offset > nextOffset) {
      messageBuffer.clear().flip(); messageBuffer
    }
    else getFromMemory(offset)
  }

  override def size(): Long = nextOffset

  override def flush(): Unit = {}

  private def getFromMemory(offset: Long): (ByteBuffer) = {
    val currentPosition = memoryBuffer.position()
    val size = {
      memoryBuffer.flip().position((offset - offsetAtZero).asInstanceOf[Int])
      memoryBuffer.getShort
    }
    memoryBuffer.get(bytes, 0, size).clear().position(currentPosition)
    messageBuffer.clear().limit(size)
    messageBuffer.put(bytes, 0, size).flip()
    messageBuffer
  }

  private def calculateOffsetAndSetBufferPosition(size: Int): Int = {
    val offset = nextOffset
    nextOffset += size

    if (memoryBuffer.position() + size > bufferSize) {
      memoryBuffer.clear()
      offsetAtZero = offset
    }
    offset
  }

  private def setSizeBuffer(size: Short): Unit = {
    sizeBuffer.clear()
    sizeBuffer.putShort(size)
    sizeBuffer.flip()
  }
}

object CircularByteBank {
  private[CircularByteBank] val defaultMemorySize = 1024 * 128
  private[CircularByteBank] val minimumSize = DataConstants.udpMaxPayload * 5
}
