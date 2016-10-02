package me.breidenbach.scatena.sequencer

import java.nio.ByteBuffer

import me.breidenbach.scatena.bank.ByteBank
import me.breidenbach.scatena.messages.Message
import me.breidenbach.scatena.util.{BufferFactory, DataConstants}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
object SequencerCore {

  private var maybeByteBank: Option[ByteBank] = None
  private val writeBuffer = BufferFactory.createBuffer()

  def setByteBank(sessionId: Long, byteBank: ByteBank): Try[Unit] = maybeByteBank match {
    case None =>
      maybeByteBank = Some(byteBank)
      writeBuffer.clear()
      writeBuffer.putLong(sessionId)
      Success()
    case _ => Failure(SequencerException("byte bank and session ID already set"))
  }

  def reset(): Unit = {
    maybeByteBank = None
  }

  def sequence(message: Message): Try[ByteBuffer] = maybeByteBank match {
    case Some(bank: ByteBank) =>
      val (sequenceNumber, size) = bank.add(message.serialize())
      writeBuffer.clear()
      writeBuffer.position(DataConstants.messageSequencePos)
      writeBuffer.putLong(sequenceNumber)
      writeBuffer.position(DataConstants.messageSizePos)
      writeBuffer.putShort(size)
      writeBuffer.position(DataConstants.messageDataStart)
      writeBuffer.put(message.serialize())
      writeBuffer.flip()
      Success(writeBuffer)
    case _ => Failure(SequencerException("no byte bank or session ID set"))
  }
}