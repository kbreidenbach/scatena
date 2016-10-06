package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
trait ByteBank {
  def reset(): Unit
  def add(buffer: ByteBuffer): (Long, Short)
  def add(bytes: Array[Byte]): (Long, Short)
  def get(offset: Long): (ByteBuffer, Short)
  def size(): Long
}

case class ByteBankException(message: String, cause: Throwable = null) extends Exception(message, cause)
