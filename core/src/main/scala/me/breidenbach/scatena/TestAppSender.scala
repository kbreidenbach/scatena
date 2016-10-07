package me.breidenbach.scatena

import java.nio.charset.StandardCharsets

import me.breidenbach.scatena.junctura.UdpWriteJunctura
import me.breidenbach.scatena.util.BufferFactory

import scala.io.StdIn

/**
  * @author Kevin Breidenbach
  *         Date: 10/2/16.
  */
object TestAppSender extends App {
  val channel = new UdpWriteJunctura("test1", "224.0.0.0", 15000, "lo0")
  val byteBuffer = BufferFactory.createBuffer()

  var data = StdIn.readLine()
  while(!data.equals("end")) {
    byteBuffer.clear()
    byteBuffer.put(data.getBytes(StandardCharsets.UTF_8))
    byteBuffer.flip()
    channel.send(byteBuffer)
    data = StdIn.readLine()
  }
}
