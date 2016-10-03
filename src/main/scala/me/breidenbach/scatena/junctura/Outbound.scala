package me.breidenbach.scatena.junctura

import java.nio.ByteBuffer

/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
trait Outbound {
  def send(buffer: ByteBuffer): Unit
  def close()
}
