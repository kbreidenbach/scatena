package me.breidenbach.scatena.util

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
object BufferFactory {
  import DataConstants.udpMaxPayload

  def createBuffer(size: Int): ByteBuffer = {
    ByteBuffer.allocate(size)
  }
  def createBuffer(): ByteBuffer = {
    createBuffer(udpMaxPayload)
  }
}
