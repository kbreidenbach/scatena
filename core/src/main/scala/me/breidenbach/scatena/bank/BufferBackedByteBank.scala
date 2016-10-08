package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.{BufferFactory, DataConstants}
import me.breidenbach.scatena.util.DataConstants._

/**
  * @author Kevin Breidenbach
  *         Date: 10/7/16
  */
abstract class BufferBackedByteBank(bufferSize: Int) extends ByteBank {

  protected val memoryBuffer = BufferFactory.createBuffer(bufferSize)
  protected val messageBuffer = BufferFactory.createBuffer()
  protected val sizeBuffer = BufferFactory.createBuffer(shortSize)
  protected val bytes = Array.ofDim[Byte](DataConstants.udpMaxPayload)

  override def add(bytes: Array[Byte]): Long = {
    if (bytes.length > bufferSize) -1 else {
      messageBuffer.clear()
      messageBuffer.put(bytes)
      messageBuffer.flip()
      add(messageBuffer)
    }
  }

  protected def getFromMemory(offset: Long): ByteBuffer = {
    val currentPosition = memoryBuffer.position()
    val size = {
      memoryBuffer.flip().position(offset.asInstanceOf[Int])
      memoryBuffer.getShort
    }
    memoryBuffer.get(bytes, 0, size).clear().position(currentPosition)
    messageBuffer.clear().limit(size)
    messageBuffer.put(bytes, 0, size).flip()
    messageBuffer
  }

  protected def setSizeBuffer(size: Short): Unit = {
    sizeBuffer.clear()
    sizeBuffer.putShort(size)
    sizeBuffer.flip()
  }


  protected def emptyBuffer() = BufferBackedByteBank.emptyBuffer
}

object BufferBackedByteBank {
  val emptyBuffer = BufferFactory.emptyReadOnlyBuffer()
}
