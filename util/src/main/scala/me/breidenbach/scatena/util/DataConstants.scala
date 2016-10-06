package me.breidenbach.scatena.util

/**
  * @author kbreiden 
  *         Date: 10/6/16
  */
object DataConstants {
  // type sizes in bytes
  val byteSize: Short = 1
  val shortSize: Short = 2
  val intSize: Short = 4
  val longSize: Short = 8

  val ethernetMaxPayload : Short= 1500
  val ipMinHeaderSize: Short = 20
  val ipMaxPayload: Short = (ethernetMaxPayload - ipMinHeaderSize).asInstanceOf[Short]
  val udpHeaderSize: Short = 8
  val udpMaxPayload: Short = (ipMaxPayload - udpHeaderSize).asInstanceOf[Short]
}
