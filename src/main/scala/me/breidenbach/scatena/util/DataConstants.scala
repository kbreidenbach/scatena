package me.breidenbach.scatena.util

/**
  * @author kbreidenbach 
  *         Date: 10/1/16.
  */
object DataConstants {
  val shortSize: Short = 2
  val intSize: Short = 4
  val longSize: Short = 8

  val ethernetMaxPayload : Short= 1500
  val ipMinHeaderSize: Short = 20
  val ipMaxPayload: Short = (ethernetMaxPayload - ipMinHeaderSize).asInstanceOf[Short]
  val udpHeaderSize: Short = 8
  val udpMaxPayload: Short = (ipMaxPayload - udpHeaderSize).asInstanceOf[Short]
  val messageHeaderPos: Short = 0
  val messageSessionPos: Short = messageHeaderPos
  val messageSessionLength: Short = longSize
  val messageSequencePos: Short = (messageSessionPos + messageSessionLength).asInstanceOf[Short]
  val messageSequenceLength: Short = longSize
  val messageSizePos: Short = (messageSequencePos + messageSequenceLength).asInstanceOf[Short]
  val messageSizeLength: Short = shortSize
  val messageDataStart: Short = (messageSizePos + messageSizeLength).asInstanceOf[Short]
}
