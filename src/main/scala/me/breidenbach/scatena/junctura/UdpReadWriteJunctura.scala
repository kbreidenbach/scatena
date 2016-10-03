package me.breidenbach.scatena.junctura
import java.net._
import java.nio.ByteBuffer
import java.nio.channels.{DatagramChannel, MembershipKey}

import me.breidenbach.scatena.util.BufferFactory
import org.slf4j.LoggerFactory

/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
@throws[JuncturaException]
class UdpReadWriteJunctura(val name: String, multicastAddress: String, port: Int, multicastInterface: String,
                           reader: Boolean = true, writer: Boolean = true) extends Inbound with Outbound {
  import UdpReadWriteJunctura.logger

  private val group = InetAddress.getByName(multicastAddress)
  private val networkInterface = NetworkInterface.getByName(multicastInterface)
  private val receiveBuffer = BufferFactory.createBuffer()
  private var maybeDatagramChannel: Option[DatagramChannel] = None
  private var maybeKey: Option[MembershipKey] = None

  getChannel

  @throws[JuncturaException]
  override def read(handler: (ByteBuffer) => Unit): Unit = {
    if (reader) {
      maybeKey.foreach(key => if (key.isValid) {
        receiveBuffer.clear()
        getChannel.receive(receiveBuffer)
        receiveBuffer.flip()
        handler(receiveBuffer)
      })
    }
  }

  @throws[JuncturaException]
  override def send(buffer: ByteBuffer) = {
    if (writer) {
      try {
        maybeKey.foreach(key => if (key.isValid) getChannel.write(buffer))
      } catch {
        case t: Throwable =>
          val message = s"error writing to channel: [error=${t.getMessage}]"
          logger.error(message, t)
          throw JuncturaException(message, t)
      }
    }
  }

  override def close(): Unit = maybeDatagramChannel match {
    case Some(channel) => scala.util.control.Exception.ignoring(classOf[Throwable]) (channel.close())
    case _ =>
  }

  @throws[JuncturaException]
  private def getChannel: DatagramChannel = maybeDatagramChannel match {
    case None => openChannel()
    case Some(channel) => if (channel.isOpen) channel else openChannel()
  }

  @throws[JuncturaException]
  private def openChannel(): DatagramChannel = {
    try {
      val channel = DatagramChannel.
        open(StandardProtocolFamily.INET).
        setOption[java.lang.Boolean](StandardSocketOptions.SO_REUSEADDR, true).
        setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface)
      maybeDatagramChannel = Some(channel)
      maybeKey = Some(channel.join(group, networkInterface))

      channel.configureBlocking(false)

      if (reader) channel.bind(new InetSocketAddress(port))
      if (writer) channel.connect(new InetSocketAddress(InetAddress.getByName(multicastAddress), port))
      channel
    } catch {
      case t: Throwable =>
        val message = s"error opening UDP channel: [error=${t.getMessage}]"
        logger.error(message, t)
        throw JuncturaException(message, t)
    }
  }
}

object UdpReadWriteJunctura {
  private[UdpReadWriteJunctura] def logger = LoggerFactory.getLogger(this.getClass)

}
