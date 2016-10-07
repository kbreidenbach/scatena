package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
case class StringMessage(message: String) extends Message {
  def length(): Int = message.length
  override def serializeObject(): Array[Byte] = message.getBytes
  override def uniqueMessageId(): Int = 0
}

object StringMessage extends Message.DeSerializer[StringMessage] {
  override def deSerialize(buffer: ByteBuffer): StringMessage = {
    val bytes = Array.ofDim[Byte](buffer.remaining())
    buffer.get(bytes)
    StringMessage(new String(bytes))
  }
}
