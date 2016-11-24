package me.breidenbach.scatena.messages

import java.nio.ByteBuffer

import me.breidenbach.scatena.util.DataConstants._

/**
  * @author Kevin Breidenbach 
  * Date: 10/11/16.
  */
trait SequenceIntervalMessage {
  val bytes: Array[Byte] = Array.ofDim[Byte](longSize * 2)
  var sequences: (Long, Long) = _
  var startSeq: Long = _
  var endSeq: Long = _

  def convertToByteArray(startSequence: Long, endSequence: Long): Array[Byte] = {
    import Message.toByteArray
    val buff = toByteArray(startSequence, endSequence)
    buff
  }

  def convertToStartAndEndSequence(buffer: ByteBuffer): Unit = {
    import Message.toDoubleLong
    buffer.get(bytes)
    sequences = toDoubleLong(bytes)
    startSeq = sequences._1
    endSeq = sequences._2
  }
}
