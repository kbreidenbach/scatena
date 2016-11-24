package me.breidenbach.scatena.junctura

import java.nio.ByteBuffer

import scala.util.Try

/**
  * @author Kevin Breidenbach
  * Date: 10/7/16.
  */
trait JuncturaChannel {
  def setListener(listener: JuncturaListener): Unit
  def send(buffer: ByteBuffer): Unit
  def receive(): Unit
  def close(): Unit
}
