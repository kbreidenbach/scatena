package me.breidenbach.scatena.messages

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.DataConstants
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._


/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
object StringMessageTest {
  val sender = "test"
  val stringMessage = new StringMessage(sender, "Test Data")
}

class StringMessageTest extends BaseTest {
  import StringMessageTest._

  test("serialization") {
    val serializedBuffer = Serializer.serialize(stringMessage)
    assertThat(serializedBuffer.isSuccess, is(true))
    assertThat(serializedBuffer.get.capacity(), is(equalTo(DataConstants.udpMaxPayload.asInstanceOf[Int])))
  }

  test("deserialization") {
    val serializedBuffer = Serializer.serialize(stringMessage)
    val deserializedObject = Message.deSerialize(serializedBuffer.get) getOrElse new StringMessage(sender, "BAD")
    assertThat(deserializedObject.asInstanceOf[StringMessage].message, is(equalTo(stringMessage.message)))
  }
}

