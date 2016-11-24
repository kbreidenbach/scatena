package me.breidenbach.scatena.replay

import java.net.NetworkInterface
import java.nio.ByteBuffer

import me.breidenbach.BaseTest
import me.breidenbach.scatena.bank.{ByteBank, CircularByteBank}
import me.breidenbach.scatena.junctura.{JuncturaListener, UdpJuncturaChannel}
import me.breidenbach.scatena.messages.MessageConstants.messageDataPosition
import me.breidenbach.scatena.messages.{Message, ReplayRequestMessage, SequenceUnavailableMessage, Serializer}
import me.breidenbach.scatena.util.BufferFactory
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

import scala.util.Random

/**
  * @author Kevin Breidenbach 
  * Date: 10/17/16
  */
class ReplayerTest extends BaseTest with JuncturaListener {
  import ReplayerTest._

  var clientJuncturaChannel: UdpJuncturaChannel = _
  var replayJuncturaChannel: UdpJuncturaChannel = _
  var testSubject: Replayer = _
  var readFun: ByteBuffer => Unit = (_) => fail("unexpected message received")

  override def beforeEach(): Unit = {
    clientJuncturaChannel = new UdpJuncturaChannel(juncturaName, testHost, testPort, networkInterface)
    replayJuncturaChannel = new UdpJuncturaChannel(replayJuncturaName, testHost, testPort, networkInterface)
    testSubject = new Replayer(replayServiceName, replayJuncturaChannel, byteBank)
    byteBank.reset()
    clientJuncturaChannel.setListener(this)
  }

  override def afterEach(): Unit = {
    replayJuncturaChannel.close()
    clientJuncturaChannel.close()
  }

  override def onRead(buffer: ByteBuffer): Unit = readFun(buffer)

  test ("empty byte bank") {
    val startSequence = 112L
    val endSequence = 1231L
    val replayMessage = ReplayRequestMessage(sender, startSequence, endSequence)
    var messageCount = 1

    readFun = (buffer) => {
      def checkMessage(assertFun: Message => Unit): Unit = {
        buffer.position(messageDataPosition)
        val message = Message.deSerialize(buffer).get
        messageCount += 1
        assertFun(message)
      }
      messageCount match {
        case 1 => checkMessage(message => {
          assertThat("message 1 type check", message.isInstanceOf[ReplayRequestMessage], is(true))
          assertThat("message 1 start sequence", message.asInstanceOf[ReplayRequestMessage].startSequence,
            is(equalTo(startSequence)))
          assertThat("message 1 end sequence", message.asInstanceOf[ReplayRequestMessage].endSequence,
            is(equalTo(endSequence)))
        })
        case 2 => checkMessage(message => {
          assertThat("message 2 type check", message.isInstanceOf[SequenceUnavailableMessage], is(true))
          assertThat("message 2 start sequence", message.asInstanceOf[SequenceUnavailableMessage].startSequence,
            is(equalTo(startSequence)))
          assertThat("message 2 end sequence", message.asInstanceOf[SequenceUnavailableMessage].endSequence,
            is(equalTo(endSequence)))
          assertThat("message 2 first available",
            message.asInstanceOf[SequenceUnavailableMessage].firstAvailableSequence, is(equalTo(0L)))
          assertThat("message 2 last available",
            message.asInstanceOf[SequenceUnavailableMessage].lastAvailableSequence, is(equalTo(0L)))
        })
        case _ => fail("unexpected message")
      }
    }

    sendMessage(Serializer.serialize(replayMessage).get)

    clientJuncturaChannel.receive()
    replayJuncturaChannel.receive()
    clientJuncturaChannel.receive()
  }

  test ("byte bank with data") {
    val data = collection.mutable.Map.empty[Long, Array[Byte]]
    val size = 900
    val range = 0 until ((CircularByteBank.minimumSize / size) - 1)
    val replayMessage = ReplayRequestMessage(sender, 0, 0)
    var messageCount = 1

    range.foreach(_ => {
      val bytes = getByteArray(size)
      data += (byteBank.add(bytes) -> bytes)
    })

    val keys = data.keySet.toArray.sorted
    val startSequence = keys(1)
    val endSequence = keys(4)

    readFun = (buffer) => {
      def checkMessage(assertFun: ByteBuffer => Unit): Unit = {
        assertFun(buffer)
      }
      messageCount match {
        case 1 => checkMessage(buffer => {
          buffer.position(messageDataPosition)
          val message = Message.deSerialize(buffer).get
          messageCount += 1
          assertThat("message 1 type check", message.isInstanceOf[ReplayRequestMessage], is(true))
          assertThat("message 1 start sequence", message.asInstanceOf[ReplayRequestMessage].startSequence,
            is(equalTo(startSequence)))
          assertThat("message 1 end sequence", message.asInstanceOf[ReplayRequestMessage].endSequence,
            is(equalTo(endSequence)))
        })
        case x if x > 1 && x <= 5 => checkMessage(buffer => {
          buffer.position(messageDataPosition)
          val foundBytes = Array.ofDim[Byte](buffer.remaining())
          val storedBytes = data(keys(x - 1))
          messageCount += 1
          buffer.get(foundBytes)
          assertThat(foundBytes, is(equalTo(storedBytes)))
        })
        case _ => fail("unexpected message")
      }
    }

    replayMessage.startSequence = startSequence
    replayMessage.endSequence = endSequence
    sendMessage(Serializer.serialize(replayMessage).get)

    clientJuncturaChannel.receive()
    replayJuncturaChannel.receive()
    clientJuncturaChannel.receive()
  }

  test ("circular byte bank that has rotated") {
    val data = collection.mutable.Map.empty[Long, Array[Byte]]
    val size = 900
    val range = 0 until ((CircularByteBank.minimumSize / size) * 2.5).asInstanceOf[Int]
    val replayMessage = ReplayRequestMessage(sender, 0, 0)
    var messageCount = 1

    range.foreach(_ => {
      val bytes = getByteArray(size)
      data += (byteBank.add(bytes) -> bytes)
    })

    val keys = data.keySet.toArray.sorted
    val firstKey = {
      var index = 0
      while (keys(index) < byteBank.firstOffset()) index += 1
      index
    }
    val startSequence = keys(firstKey + 2)
    val endSequence = keys(firstKey + 5)

    readFun = (buffer) => {
      def checkMessage(assertFun: ByteBuffer => Unit): Unit = {
        assertFun(buffer)
      }
      messageCount match {
        case 1 => checkMessage(buffer => {
          buffer.position(messageDataPosition)
          val message = Message.deSerialize(buffer).get
          messageCount += 1
          assertThat("message 1 type check", message.isInstanceOf[ReplayRequestMessage], is(true))
          assertThat("message 1 start sequence", message.asInstanceOf[ReplayRequestMessage].startSequence,
            is(equalTo(startSequence)))
          assertThat("message 1 end sequence", message.asInstanceOf[ReplayRequestMessage].endSequence,
            is(equalTo(endSequence)))
        })
        case x if x > 1 && x <= 2 => checkMessage(buffer => {
          buffer.position(messageDataPosition)
          val foundBytes = Array.ofDim[Byte](buffer.remaining())
          val storedBytes = data(keys(x - 1))
          messageCount += 1
          buffer.get(foundBytes)
          assertThat(foundBytes, is(equalTo(storedBytes)))
        })
        case _ => fail("unexpected message")
      }
    }

    replayMessage.startSequence = startSequence
    replayMessage.endSequence = endSequence
    sendMessage(Serializer.serialize(replayMessage).get)

    clientJuncturaChannel.receive()
    replayJuncturaChannel.receive()
    clientJuncturaChannel.receive()
  }

  test ("circular byte bank that has rotated with replay request out of bounds") {
    val data = collection.mutable.Map.empty[Long, Array[Byte]]
    val size = 900
    val range = 0 until ((CircularByteBank.minimumSize / size) * 2.5).asInstanceOf[Int]
    val replayMessage = ReplayRequestMessage(sender, 0, 0)
    var messageCount = 1

    range.foreach(_ => {
      val bytes = getByteArray(size)
      data += (byteBank.add(bytes) -> bytes)
    })

    val keys = data.keySet.toArray.sorted
    val startSequence = keys(1)
    val endSequence = keys(4)

    readFun = (buffer) => {
      def checkMessage(assertFun: Message => Unit): Unit = {
        buffer.position(messageDataPosition)
        val message = Message.deSerialize(buffer).get
        messageCount += 1
        assertFun(message)
      }
      messageCount match {
        case 1 => checkMessage(message => {
          assertThat("message 1 type check", message.isInstanceOf[ReplayRequestMessage], is(true))
          assertThat("message 1 start sequence", message.asInstanceOf[ReplayRequestMessage].startSequence,
            is(equalTo(startSequence)))
          assertThat("message 1 end sequence", message.asInstanceOf[ReplayRequestMessage].endSequence,
            is(equalTo(endSequence)))
        })
        case 2 => checkMessage(message => {
          assertThat("message 2 type check", message.isInstanceOf[SequenceUnavailableMessage], is(true))
          assertThat("message 2 start sequence", message.asInstanceOf[SequenceUnavailableMessage].startSequence,
            is(equalTo(startSequence)))
          assertThat("message 2 end sequence", message.asInstanceOf[SequenceUnavailableMessage].endSequence,
            is(equalTo(endSequence)))
          assertThat("message 2 first available",
            message.asInstanceOf[SequenceUnavailableMessage].firstAvailableSequence, is(equalTo(byteBank.firstOffset())))
          assertThat("message 2 last available",
            message.asInstanceOf[SequenceUnavailableMessage].lastAvailableSequence, is(equalTo(byteBank.lastOffset())))
        })
        case _ => fail("unexpected message")
      }
    }

    replayMessage.startSequence = startSequence
    replayMessage.endSequence = endSequence
    sendMessage(Serializer.serialize(replayMessage).get)

    clientJuncturaChannel.receive()
    replayJuncturaChannel.receive()
    clientJuncturaChannel.receive()
  }

  test ("circular byte bank that has rotated and request past last sequence") {
    val data = collection.mutable.Map.empty[Long, Array[Byte]]
    val size = 900
    val range = 0 until ((CircularByteBank.minimumSize / size) * 2.5).asInstanceOf[Int]
    val replayMessage = ReplayRequestMessage(sender, 0, 0)
    var messageCount = 1

    range.foreach(_ => {
      val bytes = getByteArray(size)
      data += (byteBank.add(bytes) -> bytes)
    })

    val keys = data.keySet.toArray.sorted
    val lastKey = {
      var index = 0
      while (keys(index) < byteBank.lastOffset()) index += 1
      index
    }
    val startSequence = keys(lastKey - 2)
    val endSequence = keys(lastKey) + 2100

    readFun = (buffer) => {
      def checkMessage(assertFun: ByteBuffer => Unit): Unit = {
        assertFun(buffer)
      }
      messageCount match {
        case 1 => checkMessage(buffer => {
          buffer.position(messageDataPosition)
          val message = Message.deSerialize(buffer).get
          messageCount += 1
          assertThat("message 1 type check", message.isInstanceOf[ReplayRequestMessage], is(true))
          assertThat("message 1 start sequence", message.asInstanceOf[ReplayRequestMessage].startSequence,
            is(equalTo(startSequence)))
          assertThat("message 1 end sequence", message.asInstanceOf[ReplayRequestMessage].endSequence,
            is(equalTo(endSequence)))
        })
        case x if x > 1 && x <= 4 => checkMessage(buffer => {
          buffer.position(messageDataPosition)
          val foundBytes = Array.ofDim[Byte](buffer.remaining())
          val storedBytes = data(keys(lastKey + x - 4))
          messageCount += 1
          buffer.get(foundBytes)
          assertThat(foundBytes, is(equalTo(storedBytes)))
        })
        case _ => fail("unexpected message")
      }
    }

    replayMessage.startSequence = startSequence
    replayMessage.endSequence = endSequence
    sendMessage(Serializer.serialize(replayMessage).get)

    clientJuncturaChannel.receive()
    replayJuncturaChannel.receive()
    clientJuncturaChannel.receive()
  }

  private def getByteArray(size: Int): Array[Byte] = {
    val byte = random.nextInt(Byte.MaxValue).asInstanceOf[Byte]
    Array.fill[Byte](size)(byte)
  }

  private def sendMessage(buffer: ByteBuffer): Unit = {
    sendBuffer.clear().position(messageDataPosition)
    sendBuffer.put(buffer)
    sendBuffer.flip()
    clientJuncturaChannel.send(sendBuffer)
  }
}

object ReplayerTest {
  val replayServiceName = "Replay"
  val juncturaName = "test junctura"
  val replayJuncturaName = "replay junctura"
  val sender = "test"
  val testHost = "230.1.1.1"
  val testPort = 17001
  val networkInterface: String = getLocalNetworkInterface.getName
  val sendBuffer: ByteBuffer = BufferFactory.createBuffer()
  val byteBank: ByteBank = new CircularByteBank(CircularByteBank.minimumSize)
  val random = new Random(12)

  def getLocalNetworkInterface: NetworkInterface = {
    val interfaces = NetworkInterface.getNetworkInterfaces
    var interface: NetworkInterface = null
    while (interfaces.hasMoreElements) {
      val networkInterface = interfaces.nextElement()
      if (networkInterface.getName.startsWith("lo")) interface = networkInterface
    }
    interface
  }
}