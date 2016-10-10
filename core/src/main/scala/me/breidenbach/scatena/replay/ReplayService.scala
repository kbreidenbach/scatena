package me.breidenbach.scatena.replay

import java.nio.ByteBuffer

import me.breidenbach.scatena.bank.ByteBank
import me.breidenbach.scatena.junctura.{JuncturaChannel, JuncturaListener}
import me.breidenbach.scatena.messages.MessageConstants._

/**
  * @author Kevin Breidenbach
  *         Date: 10/7/16.
  */
class ReplayService(multicastChannel: JuncturaChannel, byteBank: ByteBank) extends JuncturaListener {

  override def onRead(buffer: ByteBuffer): Unit = buffer.remaining() match {
    case x if x > messageDataPosition =>
      val flags = buffer.get(messageFlagsPosition)
      if (bitSet(flags, resendFlagPos)) handleRequest(buffer: ByteBuffer)
    case _ =>
  }

  private def handleRequest(buffer: ByteBuffer): Unit = {

  }
}
