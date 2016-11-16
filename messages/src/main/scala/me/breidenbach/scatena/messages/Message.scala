package me.breidenbach.scatena.messages

import java.io.IOException
import java.nio.ByteBuffer

import me.breidenbach.scatena.util.{BufferFactory, DataConstants}
import me.breidenbach.scatena.util.DataConstants._

import scala.util.{Failure, Success, Try}

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
object Message {
  import MessageConstants.senderNameSize
  import DataConstants.intSize

  trait DeSerializer[T <: Message] {
    def deSerialize(sender: String, buffer: ByteBuffer): T
  }

  val sender = Array.ofDim[Byte](10)
  val messageIdSize = 4
  val positionOfObjectData = intSize + senderNameSize
  val messageIdToDeserializer: Map[Int, DeSerializer[_ <: Message]] = Map(
    -2 -> SequenceUnavailableMessage,
    -1 -> ReplayRequestMessage,
    0 -> StringMessage)

  private val numberConverterBuffer = BufferFactory.createBuffer()
  private val shortBytes = Array.ofDim[Byte](shortSize)
  private val intBytes = Array.ofDim[Byte](intSize)
  private val longBytes = Array.ofDim[Byte](longSize)
  private val doubleLongBytes = Array.ofDim[Byte](longSize * 2)

  def toShort(bytes: Array[Byte]): Short = {
    toType[Short](bytes, (buffer) => buffer.getShort())
  }

  def toInt(bytes: Array[Byte]): Int = {
    toType[Int](bytes, (buffer) => buffer.getInt())
  }

  def toLong(bytes: Array[Byte]): Long = {
    toType[Long](bytes, (buffer) => buffer.getLong())
  }

  def toDoubleLong(bytes: Array[Byte]): (Long, Long) = {
    toType[(Long, Long)](bytes, (buffer) => (buffer.getLong(), buffer.getLong()))
  }

  def toByteArray(short: Short): Array[Byte] = {
    toByteArray(shortBytes, (buffer) => buffer.putShort(short))
  }

  def toByteArray(int: Int): Array[Byte] = {
    toByteArray(intBytes, (buffer) => buffer.putInt(int))
  }

  def toByteArray(long: Long): Array[Byte] = {
    toByteArray(longBytes, (buffer) => buffer.putLong(long))
  }

  def toByteArray(frontLong: Long, endLong: Long): Array[Byte] = {
    toByteArray(doubleLongBytes, (buffer) => buffer.putLong(frontLong).putLong(endLong))
  }

  private def toType[T](bytes: Array[Byte], converter: (ByteBuffer) => T): T = {
    numberConverterBuffer.clear()
    numberConverterBuffer.put(bytes).flip()
    converter(numberConverterBuffer)
  }

  private def toByteArray(bytes: Array[Byte], converter: (ByteBuffer) => Unit): Array[Byte] = {
    numberConverterBuffer.clear()
    converter(numberConverterBuffer)
    numberConverterBuffer.flip()
    numberConverterBuffer.get(bytes)
    bytes
  }

  def deSerialize(buffer: ByteBuffer): Try[Message] = {
    val messageTypeId = buffer.getInt()
    val deSerializer = {
      buffer.get(sender)
      messageIdToDeserializer.get(messageTypeId)
    }

    deSerializer.foreach(message => {
      return Success(message.deSerialize(trimSender(sender), buffer))
    })

    Failure(new IllegalArgumentException("no deserializable object found"))
  }

  def trimSender(sender: Array[Byte]): String = {
    new String(sender).trim
  }
}

trait Message {

  import Message.positionOfObjectData

  val buffer = BufferFactory.createBuffer()

  @throws[IOException]
  private[messages] def serialize(): ByteBuffer = {
    if (senderName().length <= 10)
      serializeMessage()
    else
      throw new IOException("senderName overflow > 10 bytes")
  }

  private def serializeMessage(): ByteBuffer = {
    val objectBytes = serializeObject()
    buffer.clear()
    buffer.putInt(uniqueMessageId())
    buffer.put(senderName().getBytes())
    while(buffer.position() < positionOfObjectData) buffer.put(32.asInstanceOf[Byte])
    buffer.put(objectBytes)
    buffer.asReadOnlyBuffer()
    buffer.flip()
    buffer
  }

  protected def senderName(): String
  protected def serializeObject(): Array[Byte]
  protected def uniqueMessageId(): Int
}