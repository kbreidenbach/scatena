package me.breidenbach.scatena.junctura

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach
  * Date: 10/7/16.
  */
trait JuncturaListener {
  def onRead(buffer: ByteBuffer): Unit
}
