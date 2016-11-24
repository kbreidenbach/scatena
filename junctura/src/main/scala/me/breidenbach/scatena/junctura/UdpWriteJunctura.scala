package me.breidenbach.scatena.junctura

import java.net._
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

import org.slf4j.LoggerFactory

/**
  * @author Kevin Breidenbach
  * Date: 10/2/16.
  */
@throws[JuncturaException]
class UdpWriteJunctura(val name: String, multicastAddress: String, port: Int, multicastInterface: String)
  extends UdpBaseJunctura(multicastAddress, port, multicastInterface) {
  import UdpWriteJunctura.logger

  getChannel(connect)

  @throws[JuncturaException]
  def send(buffer: ByteBuffer): Unit = {
    try {
      maybeKey.foreach(key => if (key.isValid) getChannel(connect).write(buffer))
    } catch {
      case t: Throwable =>
        val message = s"error writing to channel: [error=${t.getMessage}]"
        logger.error(message, t)
        throw JuncturaException(message, t)
    }
  }

  private def connect(datagramChannel: DatagramChannel): DatagramChannel = {
    datagramChannel.connect(new InetSocketAddress(InetAddress.getByName(multicastAddress), port))
  }

}

object UdpWriteJunctura {
  private[UdpWriteJunctura] def logger = LoggerFactory.getLogger(this.getClass)

}
