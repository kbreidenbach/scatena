package me.breidenbach.scatena.bank

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file._

import me.breidenbach.scatena.util.BufferFactory
import org.slf4j.LoggerFactory

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
@throws(classOf[ByteBankException])
class FileByteBank(filePath: String) extends ByteBank {
  import FileByteBank._

  private val path = FileSystems.getDefault.getPath(filePath)
  private val channel = openFile()

  override def reset(): Unit = channel.truncate(0)

  override def add(buffer: ByteBuffer): Long = {
    val position = channel.size()
    channel.write(buffer)
    position
  }

  override def get(offset: Long): ByteBuffer = {
    val buffer = BufferFactory.createBuffer()
    var length: Short = 0
    channel.position(offset).read(buffer)
    length = buffer.getShort(0)
    copyBuffer(length, buffer)
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
}

object FileByteBank {
  private[FileByteBank] val fileOptions = new java.util.HashSet[OpenOption]()
  fileOptions.add(StandardOpenOption.CREATE)
  fileOptions.add(StandardOpenOption.APPEND)

  private[FileByteBank] def logger = LoggerFactory.getLogger(this.getClass)
}
