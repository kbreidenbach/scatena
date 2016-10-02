package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.BufferFactory

import scala.util.{Failure, Success, Try}

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
object Message {

  trait DeSerializer {
    def deSerialize(buffer: ByteBuffer): Message
  }

  val messageIdSize = 4
  val messageIdToDeserializer: Map[Int, DeSerializer] = Map(0 -> StringMessage)

  def deSerialize(buffer: ByteBuffer): Try[Message] = {
    val messageTypeId = buffer.getInt(0)
    val deSerializer = messageIdToDeserializer.get(messageTypeId)

    deSerializer.foreach(d => {
      buffer.position(messageIdSize)
      return Success(d.deSerialize(buffer))
    })

    Failure(new IllegalArgumentException("no deserializable object found"))
  }
}

trait Message {
  import Message.messageIdSize

  def serialize(): ByteBuffer = {
    val objectBytes = serializeObject()
    val buffer = BufferFactory.createBuffer(messageIdSize + objectBytes.length)
    buffer.putInt(uniqueMessageId())
    buffer.put(objectBytes)
    buffer.asReadOnlyBuffer()
    buffer.flip()
    buffer
  }

  def serializeObject(): Array[Byte]
  def uniqueMessageId(): Int
}