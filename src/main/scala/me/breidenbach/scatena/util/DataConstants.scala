package me.breidenbach.scatena.util

/**
  * @author kbreidenbach 
  *         Date: 10/1/16.
  */
object DataConstants {
  val byteSize: Short = 1
  val shortSize: Short = 2
  val intSize: Short = 4
  val longSize: Short = 8

  val ethernetMaxPayload : Short= 1500
  val ipMinHeaderSize: Short = 20
  val ipMaxPayload: Short = (ethernetMaxPayload - ipMinHeaderSize).asInstanceOf[Short]
  val udpHeaderSize: Short = 8
  val udpMaxPayload: Short = (ipMaxPayload - udpHeaderSize).asInstanceOf[Short]
  val messageHeaderPosition: Short = 0
  val messageSessionPosition: Short = messageHeaderPosition
  val messageSessionLength: Short = longSize
  val messageSequencePosition: Short = (messageSessionPosition + messageSessionLength).asInstanceOf[Short]
  val messageSequenceLength: Short = longSize
  val messageSizePosition: Short = (messageSequencePosition + messageSequenceLength).asInstanceOf[Short]
  val messageSizeLength: Short = shortSize
  val messageFlagsPosition: Short = (messageSizePosition + messageSizeLength).asInstanceOf[Short]
  val messageFlagsLength: Short = byteSize
  val messageDataPosition: Short = (messageFlagsPosition + messageFlagsLength).asInstanceOf[Short]
}
