package me.breidenbach.scatena.util

/**
  * @author kbreidenbach 
  *         Date: 10/1/16.
  */
object DataConstants {
  val shortSize = 2
  val intSize = 4
  val longSize = 8

  val ethernetMaxPayload = 1500
  val ipMinHeaderSize = 20
  val ipMaxPayload = ethernetMaxPayload - ipMinHeaderSize
  val udpHeaderSize = 8
  val udpMaxPayload = ipMaxPayload - udpHeaderSize
  val messageHeaderPos = 0
  val messageHeaderLength = 0
  val messageSizePos = messageHeaderPos + messageHeaderLength
  val messageSizeLength = messageSizePos + shortSize
}
