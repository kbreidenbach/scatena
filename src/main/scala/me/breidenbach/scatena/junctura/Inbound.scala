package me.breidenbach.scatena.junctura

import java.nio.ByteBuffer

/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
trait Inbound {
  def read(handler: (ByteBuffer) => Unit)
  def close()
}
