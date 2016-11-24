package me.breidenbach.scatena.util

import java.nio.ByteBuffer
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import org.slf4j.{Logger, LoggerFactory}

/**
  * @author Kevin Breidenbach
  * Date: 9/28/16.
  */
object BufferFactory {
  import DataConstants.udpMaxPayload

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val availableBuffers = new collection.mutable.ListBuffer[ByteBuffer]()
  val newBuffers = new collection.mutable.ListBuffer[ByteBuffer]()
  val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  val bufferBuilder = new BufferBuilder

  // TODO set these with configuration
  val useDirect = true
  val poolSize = 100

  bufferBuilder.run()
  executor.scheduleAtFixedRate(bufferBuilder, 100, 100, TimeUnit.MILLISECONDS)

  def createBuffer(size: Int): ByteBuffer = {
    if (size == udpMaxPayload) createBuffer()
    else if (useDirect) ByteBuffer.allocateDirect(size).order(java.nio.ByteOrder.LITTLE_ENDIAN)
    else ByteBuffer.allocate(size).order(java.nio.ByteOrder.LITTLE_ENDIAN)
  }

  def createBuffer(): ByteBuffer = {
    val buffer = if (availableBuffers.nonEmpty) availableBuffers.remove(0) else createUdpSizeBuffer()
    fillBuffer()
    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
  }

  def emptyReadOnlyBuffer(): ByteBuffer = {
    ByteBuffer.allocate(0).asReadOnlyBuffer()
  }

  private def fillBuffer(): Unit = {
    if (availableBuffers.size < poolSize / 4) newBuffers.synchronized {
      availableBuffers ++= newBuffers.take(poolSize - availableBuffers.size)
    }
  }

  private def createUdpSizeBuffer(): ByteBuffer =
    if (useDirect) ByteBuffer.allocateDirect(udpMaxPayload)
    else ByteBuffer.allocate(udpMaxPayload)

  private[BufferFactory] class BufferBuilder extends Runnable {
    override def run(): Unit = {
      var productionSize = 0
      newBuffers.synchronized {
        productionSize = poolSize - newBuffers.size
        1 to productionSize foreach(_ => newBuffers += createUdpSizeBuffer())
      }
      if (productionSize > 0) logger.info(s"created $productionSize new byte buffers of UDP max packet size")
    }
  }
}