package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach 
  *         Date: 10/9/16.
  */
class ReplayRequestMessage(startSequence: Int, endSequence: Int) extends Message {
  override protected def serializeObject(): Array[Byte] = {
    null
  }

  override protected def uniqueMessageId() = -1
}

object ReplayRequestMessage extends Message.DeSerializer[StringMessage] {
  override def deSerialize(buffer: ByteBuffer): StringMessage = {
    val bytes = Array.ofDim[Byte](buffer.remaining())
    buffer.get(bytes)
    StringMessage(new String(bytes))
  }
}
