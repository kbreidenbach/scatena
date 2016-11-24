package me.breidenbach.scatena.messages

/**
  * @author Kevin Breidenbach
  * Date: 10/1/16.
  */
object MessageConstants {
  import me.breidenbach.scatena.util.DataConstants._

  // flag positions in the byte
  val resendFlagPos: Byte = 0
  val senderNameSize: Int = 10

  // communication buffer positions and sizes
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

  def setBit(byte: Byte, position: Byte): Byte = (byte | (1 << position)).asInstanceOf[Byte]

  def bitSet(byte: Byte, position: Byte): Boolean = ((byte >> position) & 1) == 1
}
