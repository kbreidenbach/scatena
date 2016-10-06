package me.breidenbach.scatena.junctura

/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
case class JuncturaException(message: String, cause: Throwable = null) extends Exception(message, cause)