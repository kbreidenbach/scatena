package me.breidenbach.scatena.sequencer

/**
  * @author Kevin Breidenbach
  *         Date: 10/2/16.
  */
case class SequencerException(message: String, cause: Throwable = null) extends Exception(message, cause)
