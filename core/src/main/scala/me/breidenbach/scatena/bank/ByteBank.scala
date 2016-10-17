package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.DataConstants
import me.breidenbach.scatena.util.DataConstants.shortSize

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
trait ByteBank {
  /**
    * add data to the bank
    * @param buffer the byte buffer containing the data. Note: data is added from current position to limit
    * @return offset in bank or -1 if it can't be stored
    */
  def add(buffer: ByteBuffer): Long

  /**
    * add data to the bank
    * @param bytes a byte array containing the data. Note: all data from array is added
    * @return
    */
  def add(bytes: Array[Byte]): Long
  def firstOffset(): Long
  def lastOffset(): Long
  def get(offset: Long): ByteBuffer
  def size(): Long
  def flush(): Unit
  def reset(): Unit
  def findNextOffset(offset: Long, size: Int): Long = offset + size + shortSize
}

case class ByteBankException(message: String, cause: Throwable = null) extends Exception(message, cause)
