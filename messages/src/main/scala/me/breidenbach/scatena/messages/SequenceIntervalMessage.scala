package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.DataConstants._

/**
  * @author Kevin Breidenbach 
  *         Date: 10/11/16.
  */
trait SequenceIntervalMessage {
  val bytes = Array.ofDim[Byte](longSize)
  var startAndEnd: Long = _
  var startSeq: Int = _
  var endSeq: Int = _

  def convertToByteArray(startSequence: Int, endSequence: Int): Array[Byte] = {
    import Message.toByteArray
    startAndEnd = (startSequence.asInstanceOf[Long] << 32) | ( endSequence & 0xffffffffL)
    toByteArray(startAndEnd)
  }

  def convertToStartAndEndSequence(buffer: ByteBuffer): Unit = {
    import Message.toLong
    buffer.get(bytes)
    startAndEnd = toLong(bytes)
    startSeq = (startAndEnd >> 32).asInstanceOf[Int]
    endSeq = startAndEnd.asInstanceOf[Int]
  }
}
