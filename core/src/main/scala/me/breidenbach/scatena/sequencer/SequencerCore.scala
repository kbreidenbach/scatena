package me.breidenbach.scatena.sequencer

import java.nio.ByteBuffer

import me.breidenbach.scatena.bank.ByteBank
import me.breidenbach.scatena.messages.{Message, MessageConstants}
import me.breidenbach.scatena.util.BufferFactory
import org.slf4j.LoggerFactory

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * @author Kevin Breidenbach
  *         Date: 10/2/16.
  */
object SequencerCore {

  private var maybeByteBank: Option[ByteBank] = None
  private val writeBuffer = BufferFactory.createBuffer()
  private val logger = LoggerFactory.getLogger(this.getClass)

  def setByteBank(sessionId: Long, byteBank: ByteBank): Try[Unit] = maybeByteBank match {
    case None =>
      maybeByteBank = Some(byteBank)
      writeBuffer.clear()
      writeBuffer.putLong(sessionId)
      Success(None)
    case _ =>
      val error = "byte bank and session ID already set"
      logger.error(error)
      Failure(SequencerException(error))
  }

  def reset(): Unit = {
    maybeByteBank = None
  }

  def sequence(message: Message): Try[ByteBuffer] = maybeByteBank match {
    case Some(bank: ByteBank) =>
      val serializedMessage = message.serialize()
      val sequenceNumber = bank.add(message.serialize())
      writeBuffer.clear()
      writeBuffer.position(MessageConstants.messageSequencePosition)
      writeBuffer.putLong(sequenceNumber)
      writeBuffer.position(MessageConstants.messageSizePosition)
      writeBuffer.putShort(serializedMessage.remaining().asInstanceOf[Short])
      writeBuffer.position(MessageConstants.messageDataPosition)
      writeBuffer.put(serializedMessage)
      writeBuffer.flip()
      Success(writeBuffer)
    case _ => Failure(SequencerException("no byte bank or session ID set"))
  }
}