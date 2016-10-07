package me.breidenbach.scatena.junctura

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

import me.breidenbach.scatena.util.BufferFactory

/**
  * @author Kevin Breidenbach
  *         Date: 10/2/16.
  */
@throws[JuncturaException]
class UdpReadJunctura(val name: String, multicastAddress: String, port: Int, multicastInterface: String)
  extends UdpBaseJunctura(multicastAddress, port, multicastInterface) {

  private val receiveBuffer = BufferFactory.createBuffer()

  getChannel(bindChannel)

  @throws[JuncturaException]
  def read(handler: (ByteBuffer) => Unit): Unit = {
    maybeKey.foreach(key => if (key.isValid) {
      receiveBuffer.clear()
      getChannel(bindChannel).receive(receiveBuffer)
      receiveBuffer.flip()
      handler(receiveBuffer)
    })
  }

  private def bindChannel(datagramChannel: DatagramChannel): DatagramChannel = {
    datagramChannel.bind(new InetSocketAddress(port))
  }
}

