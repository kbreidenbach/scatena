package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach
  * Date: 9/28/16.
  */
case class StringMessage(var sender: String, var message: Array[Byte]) extends Message() {
  def this(sender: String, stringMessage: String) = this(sender, stringMessage.getBytes)
  def length(): Int = message.length

  override protected def serializeObject(): Array[Byte] = message
  override protected def uniqueMessageId(): Int = 0
  override protected def senderName(): String = sender
}

object StringMessage extends Message.DeSerializer[StringMessage] {
  val stringMessage = StringMessage("", Array.empty[Byte])
  override def deSerialize(sender: String, buffer: ByteBuffer): StringMessage = {
    val bytes = Array.ofDim[Byte](buffer.remaining())
    buffer.get(bytes)
    stringMessage.sender = sender
    stringMessage.message = bytes
    stringMessage
  }
}
