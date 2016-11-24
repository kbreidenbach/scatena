package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.{BufferFactory, DataConstants}
import me.breidenbach.scatena.util.DataConstants._

import scala.collection.mutable.ListBuffer

/**
  * @author Kevin Breidenbach
  * Date: 10/7/16
  */
abstract class BufferBackedByteBank(bufferSize: Int) extends ByteBank {

  protected val memoryBuffer: ByteBuffer = BufferFactory.createBuffer(bufferSize)
  protected val messageBuffer: ByteBuffer = BufferFactory.createBuffer()
  protected val sizeBuffer: ByteBuffer = BufferFactory.createBuffer(shortSize)
  protected val bytes: Array[Byte] = Array.ofDim[Byte](DataConstants.udpMaxPayload)
  protected var lastAddedOffset = 0L

  override def add(bytes: Array[Byte]): Long = {
    if (bytes.length > bufferSize) -1 else {
      messageBuffer.clear()
      messageBuffer.put(bytes)
      messageBuffer.flip()
      add(messageBuffer)
    }
  }

  override def lastOffset(): Long = lastAddedOffset

  protected def getFromMemory(offset: Long): ByteBuffer = {
    val currentPosition = memoryBuffer.position()
    val size = {
      memoryBuffer.flip().limit(bufferSize).position(offset.asInstanceOf[Int])
      memoryBuffer.getShort
    }
    if (size > udpMaxPayload) {
      emptyBuffer()
    } else {
      memoryBuffer.get(bytes, 0, size).clear().position(currentPosition)
      messageBuffer.clear().limit(size)
      messageBuffer.put(bytes, 0, size).flip()
      messageBuffer
    }
  }

  protected def setSizeBuffer(size: Short): Unit = {
    sizeBuffer.clear()
    sizeBuffer.putShort(size)
    sizeBuffer.flip()
  }

  protected def emptyBuffer(): ByteBuffer = BufferBackedByteBank.emptyBuffer

}

object BufferBackedByteBank {
  val emptyBuffer: ByteBuffer = BufferFactory.emptyReadOnlyBuffer()
}
