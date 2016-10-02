package me.breidenbach.scatena.sequencer

/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
case class SequencerException(message: String, cause: Throwable = null) extends Exception(message, cause)
