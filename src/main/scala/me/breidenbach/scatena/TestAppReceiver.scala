package me.breidenbach.scatena

import java.nio.ByteBuffer

import me.breidenbach.scatena.junctura.UdpReadWriteJunctura

/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
object TestAppReceiver extends App {

  val channel = new UdpReadWriteJunctura("test1", "224.0.0.0", 15000, "lo0", writer = false)

  while(true) {
    readAndPrint()
    this.synchronized {
      wait(500)
    }
  }

  private def readAndPrint(): Unit = {
    val buffer = {
      var buf: ByteBuffer = null
      channel.read((handler) => buf = handler)
      buf
    }

    if (buffer.limit() > 0) {
      val bytes = Array.ofDim[Byte](buffer.limit())
      buffer.get(bytes)
      println(new String(bytes))
    }
  }
}
