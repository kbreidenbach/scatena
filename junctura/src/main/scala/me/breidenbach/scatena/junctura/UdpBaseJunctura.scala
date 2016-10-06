package me.breidenbach.scatena.junctura

import java.net._
import java.nio.channels.{DatagramChannel, MembershipKey}

/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
class UdpBaseJunctura(multicastAddress: String, port: Int, multicastInterface: String) {
  private val group = InetAddress.getByName(multicastAddress)
  private val networkInterface = NetworkInterface.getByName(multicastInterface)
  protected var maybeDatagramChannel: Option[DatagramChannel] = None
  protected var maybeKey: Option[MembershipKey] = None

  def close(): Unit = maybeDatagramChannel match {
    case Some(channel) => scala.util.control.Exception.ignoring(classOf[Throwable])(channel.close())
    case _ =>
  }

  @throws[JuncturaException]
  protected def getChannel(channelFun: (DatagramChannel) => DatagramChannel): DatagramChannel =
    maybeDatagramChannel match {
    case None => openChannel(channelFun)
    case Some(channel) => if (channel.isOpen) channel else openChannel(channelFun)
  }

  @throws[JuncturaException]
  protected def openChannel(channelFun: (DatagramChannel) => DatagramChannel): DatagramChannel = {
    try {
      val channel = DatagramChannel.
        open(StandardProtocolFamily.INET).
        setOption[java.lang.Boolean](StandardSocketOptions.SO_REUSEADDR, true).
        setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface)
      maybeDatagramChannel = Some(channel)
      maybeKey = Some(channel.join(group, networkInterface))

      channel.configureBlocking(false)
      channelFun(channel)
    } catch {
      case t: Throwable =>
        val message = s"error opening UDP channel: [error=${t.getMessage}]"
        throw JuncturaException(message, t)
    }
  }
}
