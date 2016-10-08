package me.breidenbach.scatena.junctura

import java.net.NetworkInterface
import java.nio.ByteBuffer

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.BufferFactory
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach
  *         Date: 10/7/16.
  */
class UdpJuncturaChannelTest extends BaseTest with JuncturaListener {
  import UdpJuncturaChannelTest._

  var testSubject: UdpJuncturaChannel = _

  override def beforeEach(): Unit = {
    sendBuffer.rewind()
    testSubject = new UdpJuncturaChannel(juncturaName, testHost, testPort, networkInterface)
    assert(testSubject.setListener(this).isSuccess, is(true))
  }

  override def afterEach(): Unit = {
    testSubject.close()
  }

  test("set listener again") {
    assertThat(testSubject.setListener(this).isFailure, is(true))
  }

  test("send and receive") {
    testSubject.send(sendBuffer)
    testSubject.receive()
  }

  override def onRead(buffer: ByteBuffer): Unit = {
    val messageBytes = Array.ofDim[Byte](buffer.remaining())
    val message = {
      buffer.get(messageBytes)
      new String(messageBytes)
    }

    assertThat("message received equals sent", message, is(equalTo(text)))
  }
}

object UdpJuncturaChannelTest {
  val networkInterface = getLocalNetworkInterface.getName
  val text = "test message"
  val message = text.getBytes
  val sendBuffer = BufferFactory.createBuffer()
  val testHost = "230.1.1.1"
  val testPort = 17000
  val badHost = "0.0.0.0"
  val badPort = 70000
  val juncturaName = "test junctura"

  sendBuffer.put(message)
  sendBuffer.flip()
  sendBuffer.asReadOnlyBuffer()

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
