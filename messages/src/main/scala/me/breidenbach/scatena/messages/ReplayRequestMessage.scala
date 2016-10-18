package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach 
  *         Date: 10/9/16.
  */
case class ReplayRequestMessage(var startSequence: Long, var endSequence: Long)
  extends Message with SequenceIntervalMessage {
  override protected def serializeObject(): Array[Byte] = {
    convertToByteArray(startSequence, endSequence)
  }

  override protected def uniqueMessageId() = -1
}

object ReplayRequestMessage extends Message.DeSerializer[ReplayRequestMessage] with SequenceIntervalMessage {

  val replayRequestMessage = ReplayRequestMessage(0, 0)

  override def deSerialize(buffer: ByteBuffer): ReplayRequestMessage = {
    convertToStartAndEndSequence(buffer)
    replayRequestMessage.startSequence = startSeq
    replayRequestMessage.endSequence = endSeq
    replayRequestMessage
  }
}
