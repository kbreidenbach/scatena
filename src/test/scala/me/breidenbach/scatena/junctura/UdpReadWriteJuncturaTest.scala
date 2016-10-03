package me.breidenbach.scatena.junctura

import java.net.{InetAddress, InetSocketAddress, NetworkInterface}
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.BufferFactory
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._
/**
  * @author kbreidenbach 
  *         Date: 10/2/16.
  */
class UdpReadWriteJuncturaTest extends BaseTest {
  import UdpReadWriteJuncturaTest._

  var subject: UdpReadWriteJunctura = _

  override def beforeEach(): Unit = {
    sendBuffer.rewind()
  }

  override def afterEach(): Unit = {
    subject.close()
  }

  test("send and receive") {
    val response = {
      var resBuffer: ByteBuffer = null
      subject = new UdpReadWriteJunctura(juncturaName, testHost, testPort, netIf)
      subject.send(sendBuffer)
      subject.read((handler) => resBuffer = handler)
      resBuffer
    }
    val messageBytes = Array.ofDim[Byte](response.remaining())
    val message = {
      response.get(messageBytes)
      new String(messageBytes)
    }

    assertThat("message received equals sent", message, is(equalTo(text)))
  }

  test("write only junctura") {
    val response = {
      var resBuffer: ByteBuffer = null
      subject = new UdpReadWriteJunctura(juncturaName, testHost, testPort, netIf, reader = false)
      subject.send(sendBuffer)
      subject.read((handler) => resBuffer = handler)
      resBuffer
    }
    assertThat(response, is(nullValue()))
  }

  test("read only junctura with junctura attempting send") {
    val response = {
      var resBuffer: ByteBuffer = null
      subject = new UdpReadWriteJunctura(juncturaName, testHost, testPort, netIf, writer = false)
      subject.send(sendBuffer)
      subject.read((handler) => resBuffer = handler)
      resBuffer
    }
    assertThat(response.limit(), is(equalTo(0)))
  }

  test("read only junctura with test sending") {
    val response = {
      var resBuffer: ByteBuffer = null
      subject = new UdpReadWriteJunctura(juncturaName, testHost, testPort, netIf, writer = false)

      val testChannel = DatagramChannel.open()
      testChannel.bind(new InetSocketAddress(testPort - 1))
      testChannel.connect(new InetSocketAddress(InetAddress.getByName(testHost), testPort))
      subject.read((handler) => resBuffer = handler)
      testChannel.write(sendBuffer)
      subject.read((handler) => resBuffer = handler)
      resBuffer
    }
    val messageBytes = Array.ofDim[Byte](response.remaining())
    val message = {
      response.get(messageBytes)
      new String(messageBytes)
    }

    assertThat("message received equals sent", message, is(equalTo(text)))
  }

  test("bad port") {
    assertThrows[JuncturaException] { new UdpReadWriteJunctura(juncturaName, testHost, badPort, netIf) }
  }

  test("bad host") {
    assertThrows[JuncturaException] { new UdpReadWriteJunctura(juncturaName, badHost, testPort, netIf)}
  }
}

object UdpReadWriteJuncturaTest {
  val netIf = getLocalNetworkInterface.getName
  val text = "test message"
  val message = text.getBytes
  val sendBuffer = BufferFactory.createBuffer()
  val testHost = "230.1.1.1"
  val testPort = 17000
  val badHost = "google.com"
  val badPort = 70000
  val juncturaName = "test junctura"

  sendBuffer.put(message)
  sendBuffer.flip()
  sendBuffer.asReadOnlyBuffer()

  def getLocalNetworkInterface: NetworkInterface = {
    val interfaces = NetworkInterface.getNetworkInterfaces
    var interface: NetworkInterface = null
    while(interfaces.hasMoreElements) {
      val networkInterface = interfaces.nextElement()
      if (networkInterface.getName.startsWith("lo")) interface = networkInterface
    }
    interface
  }
}
