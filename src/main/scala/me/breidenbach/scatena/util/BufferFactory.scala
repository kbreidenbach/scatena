package me.breidenbach.scatena.util

import java.nio.ByteBuffer

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
object BufferFactory {
  def createBuffer(size: Int): ByteBuffer = {
    ByteBuffer.allocate(size)
  }
}
