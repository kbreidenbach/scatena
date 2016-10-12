package me.breidenbach.scatena.messages

import me.breidenbach.BaseTest
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._


/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
object StringMessageTest {
  val stringMessage = new StringMessage("Test Data")
}

class StringMessageTest extends BaseTest {
  import StringMessageTest._

  test("serialization") {
    val serializedBuffer = Serializer.serialize(stringMessage)
    assertThat(serializedBuffer.isLeft, is(true))
    assertThat(serializedBuffer.left.get.capacity(), is(equalTo(stringMessage.length() + Message.messageIdSize)))
  }

  test("deserialization") {
    val serializedBuffer = Serializer.serialize(stringMessage)
    val deserializedObject = Message.deSerialize(serializedBuffer.left.get) getOrElse new StringMessage("BAD")
    assertThat(deserializedObject.asInstanceOf[StringMessage].message, is(equalTo(stringMessage.message)))
  }
}

