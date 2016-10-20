package me.breidenbach.scatena.replay

import java.nio.ByteBuffer

import me.breidenbach.scatena.bank.ByteBank
import me.breidenbach.scatena.junctura.{JuncturaChannel, JuncturaListener}
import me.breidenbach.scatena.messages.MessageConstants._
import me.breidenbach.scatena.messages.{Message, ReplayRequestMessage, SequenceUnavailableMessage, Serializer}
import me.breidenbach.scatena.util.BufferFactory
import me.breidenbach.scatena.util.DataConstants.udpMaxPayload

import scala.util.Success

/**
  * @author Kevin Breidenbach
  *         Date: 10/7/16.
  */
class ReplayService(multicastChannel: JuncturaChannel, byteBank: ByteBank) extends JuncturaListener {

  private val sequenceUnavailableMessage = SequenceUnavailableMessage(0, 0, 0, 0)
  private val sendBuffer = BufferFactory.createBuffer()
  private val resendFlags = 0.asInstanceOf[Byte]
  private val notResenfFlags = 0.asInstanceOf[Byte]

  setBit(resendFlagPos, resendFlags)
  multicastChannel.setListener(this)

  override def onRead(buffer: ByteBuffer): Unit = buffer.remaining() match {
    case x if x > messageDataPosition =>
      if (!bitSet(buffer.get(messageFlagsPosition), resendFlagPos))
        handleRequest({buffer.position(messageDataPosition); buffer})
    case _ =>
  }

  private def handleRequest(buffer: ByteBuffer): Unit = Message.deSerialize(buffer) match {
    case Success(request: ReplayRequestMessage) => processRequest(request)
    case _ =>
  }

  private def processRequest(replayRequestMessage: ReplayRequestMessage): Unit = {
    val startSequence = replayRequestMessage.startSequence
    val endSequence = replayRequestMessage.endSequence
    if (startSequence < byteBank.firstOffset()) {
      sendSequenceUnavailableMessage(startSequence,
        if (endSequence < byteBank.firstOffset()) endSequence else byteBank.firstOffset())
    }
    if (endSequence > byteBank.firstOffset()) {
      get(
        if (startSequence > byteBank.firstOffset()) startSequence else byteBank.firstOffset(),
        if (endSequence < byteBank.lastOffset()) endSequence else byteBank.lastOffset())
    }
  }

  private def get(startOffset: Long, endOffset: Long): Unit = {
    if (startOffset <= byteBank.lastOffset() && endOffset >= byteBank.firstOffset()) {
      byteBank.get(startOffset) match {
        case buffer:ByteBuffer if buffer.limit() == 0 =>
          if (startOffset < endOffset) get(endOffset, endOffset)
        case buffer:ByteBuffer =>
          val nextOffset = byteBank.findNextOffset(startOffset, buffer.remaining())
          sendMessgae(buffer, resendFlags)
          if (nextOffset <= endOffset) get(nextOffset, endOffset)
      }
    }
  }

  private def sendSequenceUnavailableMessage(startSequence: Long, endSequence: Long): Unit = {
    sequenceUnavailableMessage.startSequence = startSequence
    sequenceUnavailableMessage.endSequence = endSequence
    sequenceUnavailableMessage.firstAvailableSequence = byteBank.firstOffset()
    sequenceUnavailableMessage.lastAvailableSequence = byteBank.lastOffset()
    Serializer.serialize(sequenceUnavailableMessage).foreach( buffer => sendMessgae(buffer, notResenfFlags))
  }

  private def sendMessgae(buffer: ByteBuffer, flags: Byte): Unit = {
    if (buffer.remaining() + messageDataPosition <= udpMaxPayload) {
      sendBuffer.clear().position(messageDataPosition)
      sendBuffer.put(messageFlagsPosition, flags)
      sendBuffer.put(buffer).flip()
      multicastChannel.send(sendBuffer)
    }
  }
}
