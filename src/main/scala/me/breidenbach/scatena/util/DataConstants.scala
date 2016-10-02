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
  val messageHeaderLength: Short = 0
  val messageSizePos: Short = (messageHeaderPos + messageHeaderLength).asInstanceOf[Short]
  val messageSizeLength: Short = (messageSizePos + shortSize).asInstanceOf[Short]
}
