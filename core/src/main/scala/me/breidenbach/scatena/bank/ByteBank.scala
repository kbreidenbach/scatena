package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
trait ByteBank {
  def reset(): Unit
  def add(buffer: ByteBuffer): (Long)
  def add(bytes: Array[Byte]): (Long)
  def get(offset: Long): (ByteBuffer)
  def size(): Long
  def flush(): Unit
}

case class ByteBankException(message: String, cause: Throwable = null) extends Exception(message, cause)
