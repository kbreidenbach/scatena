package me.breidenbach.scatena.bank

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file._

import me.breidenbach.scatena.util.{BufferFactory, DataConstants}
import org.slf4j.LoggerFactory

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
@throws(classOf[ByteBankException])
class FileByteBank(filePath: String, bufferSize: Int = FileByteBank.defaultMemorySize,
                   flushSize: Int = FileByteBank.defaultFlushSize) extends ByteBank {
  import me.breidenbach.scatena.util.DataConstants._
  import FileByteBank._

  private val sizeBuffer = BufferFactory.createBuffer(shortSize)
  private val memoryBuffer = BufferFactory.createBuffer(bufferSize)
  private val messageBuffer = BufferFactory.createBuffer()
  private val path = FileSystems.getDefault.getPath(filePath)
  private val channel = openFile()
  private val bytes = Array.ofDim[Byte](DataConstants.udpMaxPayload)

  override def reset(): Unit = {
    channel.truncate(0)
    memoryBuffer.clear()
  }

  override def add(buffer: ByteBuffer): (Long) = {
    val size = buffer.remaining().asInstanceOf[Short]
    val bufferPosition = {
      if (memoryBuffer.position() > flushSize) flush()
      memoryBuffer.position()
    }
    setSizeBuffer(size)
    memoryBuffer.put(sizeBuffer).put(buffer)
    channel.size() + bufferPosition
  }

  override def add(bytes: Array[Byte]): (Long) = {
    messageBuffer.clear()
    messageBuffer.put(bytes)
    messageBuffer.flip()
    add(messageBuffer)
  }

  override def get(offset: Long): (ByteBuffer) = {
    val channelSize = channel.size()
    if (channelSize > 0 && offset < channelSize) getFromChannel(offset)
    else if ((offset - channelSize) < memoryBuffer.position()) getFromMemory(offset - channelSize)
    else {
      messageBuffer.clear().flip(); messageBuffer
    }
  }

  override def size(): Long = channel.size() + memoryBuffer.position()

  override def flush(): Unit = {
    channel.position(channel.size())
    memoryBuffer.flip()
    channel.write(memoryBuffer)
    memoryBuffer.clear()
  }

  private def getFromMemory(offset: Long): (ByteBuffer) = {
    val currentPosition = memoryBuffer.position()
    val size = {
      memoryBuffer.flip().position(offset.asInstanceOf[Int])
      memoryBuffer.getShort
    }
    memoryBuffer.get(bytes, 0, size).clear().position(currentPosition)
    messageBuffer.clear().limit(size)
    messageBuffer.put(bytes, 0, size).flip()
    messageBuffer
  }

  private def getFromChannel(offset: Long): (ByteBuffer) = {
    messageBuffer.clear()
    messageBuffer.limit(getSizeFromChannel(offset))
    channel.read(messageBuffer)
    messageBuffer.flip()
    messageBuffer
  }

  private def getSizeFromChannel(offset: Long): Short = {
    val size = {
      sizeBuffer.clear()
      channel.position(offset)
      channel.read(sizeBuffer)
      sizeBuffer.flip()
      sizeBuffer.getShort()
    }
    if (size < udpMaxPayload) size else udpMaxPayload
  }

  private def setSizeBuffer(size: Short): Unit = {
    sizeBuffer.clear()
    sizeBuffer.putShort(size)
    sizeBuffer.flip()
  }

  private def openFile(): SeekableByteChannel = {
    try {
      Files.deleteIfExists(path)
      Files.newByteChannel(path, fileOptions)
    } catch {
      case e: IOException =>
        val message = s"cannot create ByteBank [error=${e.getMessage}]"
        logger.error(message, e)
        throw ByteBankException(message, e)
    }
  }
}

object FileByteBank {
  private[FileByteBank] val defaultMemorySize = 1024 * 128
  private[FileByteBank] val defaultFlushSize = 1024 * 112
  private[FileByteBank] def logger = LoggerFactory.getLogger(this.getClass)
  private[FileByteBank] val fileOptions = new java.util.HashSet[OpenOption]()
  fileOptions.add(StandardOpenOption.CREATE)
  fileOptions.add(StandardOpenOption.WRITE)
  fileOptions.add(StandardOpenOption.READ)
}
