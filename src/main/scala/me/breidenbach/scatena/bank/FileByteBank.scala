package me.breidenbach.scatena.bank

import java.nio.ByteBuffer

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
class FileByteBank extends ByteBank {
  override def reset(): Unit = ???

  override def add(buffer: _root_.java.nio.ByteBuffer): Long = ???

  override def get(offset: Long): ByteBuffer = ???
}
