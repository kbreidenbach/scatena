package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
trait ByteBank {
  def reset(): Unit
  def add(buffer: ByteBuffer): Long
  def get(offset: Long): ByteBuffer
}
