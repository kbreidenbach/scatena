package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach 
  *         Date: 10/11/16.
  */
case class SequenceUnavailableMessage (var sender: String, var startSequence: Long, var endSequence: Long,
                                       var firstAvailableSequence: Long, var lastAvailableSequence: Long)
  extends Message() with SequenceIntervalMessage {

  override protected def serializeObject(): Array[Byte] = {
    convertToByteArray(startSequence, endSequence).clone() ++
      convertToByteArray(firstAvailableSequence, lastAvailableSequence)
  }

  override protected def uniqueMessageId() = -2
  override protected def senderName(): String = sender
}

object SequenceUnavailableMessage extends Message.DeSerializer[SequenceUnavailableMessage]
  with SequenceIntervalMessage {

  val sequenceUnavailableMessage = SequenceUnavailableMessage("", 0, 0, 0, 0)

  override def deSerialize(sender: String, buffer: ByteBuffer): SequenceUnavailableMessage = {
    convertToStartAndEndSequence(buffer)
    sequenceUnavailableMessage.sender = sender
    sequenceUnavailableMessage.startSequence = startSeq
    sequenceUnavailableMessage.endSequence = endSeq
    convertToStartAndEndSequence(buffer)
    sequenceUnavailableMessage.firstAvailableSequence = startSeq
    sequenceUnavailableMessage.lastAvailableSequence = endSeq
    sequenceUnavailableMessage
  }
}