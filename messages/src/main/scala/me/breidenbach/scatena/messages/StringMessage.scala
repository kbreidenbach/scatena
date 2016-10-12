package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
case class StringMessage(var message: Array[Byte]) extends Message {
  def this(stringMessage: String) = this(stringMessage.getBytes)
  def length(): Int = message.length
  override protected def serializeObject(): Array[Byte] = message
  override protected def uniqueMessageId(): Int = 0
}

object StringMessage extends Message.DeSerializer[StringMessage] {
  val stringMessage = StringMessage(Array.empty[Byte])
  override def deSerialize(buffer: ByteBuffer): StringMessage = {
    val bytes = Array.ofDim[Byte](buffer.remaining())
    buffer.get(bytes)
    stringMessage.message = bytes
    stringMessage
  }
}
