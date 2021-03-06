package me.breidenbach.scatena.junctura

import java.net.NetworkInterface
import java.nio.ByteBuffer

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.BufferFactory
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._
/**
  * @author Kevin Breidenbach
  * Date: 10/2/16.
  */
class UdpJuncturaTest extends BaseTest {
  import UdpJuncturaTest._

  var writer: UdpWriteJunctura = _
  var receiver: UdpReadJunctura = _

  override def beforeEach(): Unit = {
    sendBuffer.rewind()
  }

  override def afterEach(): Unit = {
    writer.close()
  }

  test("send and receive") {
    val response = {
      var resBuffer: ByteBuffer = null
      writer = new UdpWriteJunctura(juncturaName, testHost, testPort, networkInterface)
      receiver = new UdpReadJunctura(juncturaName, testHost, testPort, networkInterface)
      writer.send(sendBuffer)
      receiver.read((handler) => resBuffer = handler)
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
    assertThrows[JuncturaException] (new UdpWriteJunctura(juncturaName, testHost, badPort, networkInterface))
  }

  test("bad host") {
    assertThrows[JuncturaException] (new UdpWriteJunctura(juncturaName, badHost, testPort, networkInterface))
  }
}

object UdpJuncturaTest {
  val juncturaName = "test junctura"
  val text = "test message"
  val testHost = "230.1.1.1"
  val testPort = 17000
  val badHost = "0.0.0.0"
  val badPort = 70000
  val networkInterface: String = getLocalNetworkInterface.getName
  val message: Array[Byte] = text.getBytes
  val sendBuffer: ByteBuffer = BufferFactory.createBuffer()

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
