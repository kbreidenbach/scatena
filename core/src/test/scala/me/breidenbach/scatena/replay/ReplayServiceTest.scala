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

/**
  * @author Kevin Breidenbach 
  *         Date: 10/17/16.
  */
class ReplayServiceTest extends BaseTest with JuncturaListener {
  import ReplayServiceTest._

  val clientJuncturaChannel = new UdpJuncturaChannel(juncturaName, testHost, testPort, networkInterface)
  val replayJuncturaChannel = new UdpJuncturaChannel(replayJuncturaName, testHost, testPort, networkInterface)
  var testSubject: ReplayService = new ReplayService(replayJuncturaChannel, byteBank)
  var readFun: ByteBuffer => Unit = (_) => fail("unexpected message received")

  clientJuncturaChannel.setListener(this)

  override def afterAll(): Unit = {
    replayJuncturaChannel.close()
    clientJuncturaChannel.close()
  }

  override def onRead(buffer: ByteBuffer): Unit = readFun(buffer)

  test ("empty byte bank") {
    val startSequence = 112L
    val endSequence = 1231L
    val replayMessage = ReplayRequestMessage(startSequence, endSequence)
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
          assertThat(message.isInstanceOf[ReplayRequestMessage], is(true))
          assertThat(message.asInstanceOf[ReplayRequestMessage].startSequence, is(equalTo(startSequence)))
          assertThat(message.asInstanceOf[ReplayRequestMessage].endSequence, is(equalTo(endSequence)))
        })
        case _ => fail("unexpected message")
      }
    }

    sendMessage(Serializer.serialize(replayMessage).get)

    clientJuncturaChannel.receive()
    replayJuncturaChannel.receive()

    // Should receive nothing (TODO: should this be a error message?)
    clientJuncturaChannel.receive()
  }

  test ("byte bank with data") {

  }

  test ("byte bank that has rotated") {

  }

  private def sendMessage(buffer: ByteBuffer): Unit = {
    sendBuffer.clear().position(messageDataPosition)
    sendBuffer.put(buffer)
    sendBuffer.flip()
    clientJuncturaChannel.send(sendBuffer)
  }
}

object ReplayServiceTest {
  val networkInterface = getLocalNetworkInterface.getName
  val sendBuffer = BufferFactory.createBuffer()
  val testHost = "230.1.1.1"
  val testPort = 17001
  val juncturaName = "test junctura"
  val replayJuncturaName = "replay junctura"
  val byteBank: ByteBank = new CircularByteBank(20000)

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