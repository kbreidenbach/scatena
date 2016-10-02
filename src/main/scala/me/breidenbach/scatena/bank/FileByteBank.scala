package me.breidenbach.scatena.bank

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file._

import me.breidenbach.scatena.util.{BufferFactory, DataConstants}
import org.slf4j.LoggerFactory

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
@throws(classOf[ByteBankException])
class FileByteBank(filePath: String) extends ByteBank {
  import FileByteBank._

  private val sizeBuffer = BufferFactory.createBuffer(DataConstants.shortSize)
  private val messageBuffer = BufferFactory.createBuffer()
  private val path = FileSystems.getDefault.getPath(filePath)
  private val channel = openFile()

  override def reset(): Unit = channel.truncate(0)

  override def add(buffer: ByteBuffer): (Long, Short) = {
    val position = channel.size()
    val size = buffer.remaining().asInstanceOf[Short]
    setSizeBuffer(size)
    channel.position(position)
    channel.write(sizeBuffer)
    channel.write(buffer)
    (position, size)
  }

  override def add(bytes: Array[Byte]): (Long, Short) = {
    messageBuffer.clear()
    messageBuffer.put(bytes)
    messageBuffer.flip()
    add(messageBuffer)
  }

  override def get(offset: Long): (ByteBuffer, Short) = {
    val size: Short = getSizeFromChannel(offset)
    messageBuffer.clear()
    messageBuffer.limit(size)
    channel.read(messageBuffer)
    messageBuffer.flip()
    messageBuffer.limit(size)
    (messageBuffer.duplicate(), size)
  }

  override def size(): Long = channel.size()

  private def openFile(): SeekableByteChannel = {
    try {
      Files.deleteIfExists(path)
      Files.newByteChannel(path, fileOptions)
    } catch {
      case e: IOException =>
        val message = "cannot create ByteBank"
        logger.error(message)
        throw ByteBankException(message, e)
    }
  }

  private def getSizeFromChannel(offset: Long): Short = {
    val size = {
      sizeBuffer.clear()
      channel.position(offset)
      channel.read(sizeBuffer)
      sizeBuffer.flip()
      sizeBuffer.getShort()
    }
    if (size < DataConstants.udpMaxPayload) size else DataConstants.udpMaxPayload
  }

  private def setSizeBuffer(size: Short): Unit = {
    sizeBuffer.clear()
    sizeBuffer.putShort(size)
    sizeBuffer.flip()
  }
}

object FileByteBank {
  private[FileByteBank] val fileOptions = new java.util.HashSet[OpenOption]()
  fileOptions.add(StandardOpenOption.CREATE)
  fileOptions.add(StandardOpenOption.WRITE)
  fileOptions.add(StandardOpenOption.READ)

  private[FileByteBank] def logger = LoggerFactory.getLogger(this.getClass)
}
