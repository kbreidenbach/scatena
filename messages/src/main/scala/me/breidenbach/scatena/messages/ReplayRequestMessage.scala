package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.DataConstants.longSize

/**
  * @author Kevin Breidenbach 
  *         Date: 10/9/16.
  */
case class ReplayRequestMessage(startSequence: Int, endSequence: Int) extends Message {
  override protected def serializeObject(): Array[Byte] = {
    import Message.toByteArray
    val startAndEnd = (startSequence.asInstanceOf[Long] << 32) | ( endSequence & 0xffffffffL)
    toByteArray(startAndEnd)
  }

  override protected def uniqueMessageId() = -1
}

object ReplayRequestMessage extends Message.DeSerializer[ReplayRequestMessage] {
  val bytes = Array.ofDim[Byte](longSize)
  var startAndEnd: Long = _
  var startSequence: Int = _
  var endSequence: Int = _

  override def deSerialize(buffer: ByteBuffer): ReplayRequestMessage = {
    import Message.toLong
    buffer.get(bytes)
    startAndEnd = toLong(bytes)
    startSequence = (startAndEnd >> 32).asInstanceOf[Int]
    endSequence = startAndEnd.asInstanceOf[Int]
    ReplayRequestMessage(startSequence, endSequence)
  }
}
