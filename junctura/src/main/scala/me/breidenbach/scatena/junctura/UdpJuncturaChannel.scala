package me.breidenbach.scatena.junctura
import java.nio.ByteBuffer

import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * @author Kevin Breidenbach
  *         Date: 10/7/16.
  */
class UdpJuncturaChannel(val name: String, multicastAddress: String, port: Int, multicastInterface: String)
  extends JuncturaChannel {
  import UdpJuncturaChannel.logger

  val sender = new UdpWriteJunctura(name, multicastAddress, port, multicastInterface)
  val receiver = new UdpReadJunctura(name, multicastAddress, port, multicastInterface)
  var maybeListener: Option[JuncturaListener] = None

  override def setListener(listener: JuncturaListener): Try[Unit] = maybeListener match {
    case None =>
      maybeListener = Some(listener)
      Success(None)
    case _ =>
      val error = "listener already set"
      logger.error(error)
      Failure(JuncturaException(error))
  }

  override def send(buffer: ByteBuffer): Unit = {
    sender.send(buffer)
  }

  override def receive(): Unit = {
    receiver.read(handler)
  }

  override def close(): Unit = {
    sender.close()
    receiver.close()
  }

  private def handler(buffer: ByteBuffer): Unit = maybeListener match {
    case Some(listener) =>
      if (buffer != null && buffer.remaining() > 0) {
        listener.onRead(buffer)
        receive()
      }
    case _ =>
  }
}

object UdpJuncturaChannel {
  val logger = LoggerFactory.getLogger(this.getClass)
}
