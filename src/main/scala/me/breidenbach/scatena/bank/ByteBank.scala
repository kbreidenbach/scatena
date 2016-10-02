package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.BufferFactory

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
trait ByteBank {
  def reset(): Unit
  def add(buffer: ByteBuffer): Long
  def get(offset: Long): ByteBuffer
  def size(): Long

  protected def copyBuffer(length: Short, source: ByteBuffer): ByteBuffer = {
    val bytes = Array.ofDim[Byte](length)
    val destination = BufferFactory.createBuffer(length)
    source.get(bytes, 0, length)
    destination.put(bytes)
  }
}

case class ByteBankException(message: String, cause: Throwable = null) extends Exception(message, cause)
