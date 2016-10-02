package me.breidenbach.scatena.messages

import me.breidenbach.BaseTest
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._


/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
object SerializerTest {
  val serializerTestObject = StringMessage("Test Data")
}

class SerializerTest extends BaseTest {
  import SerializerTest._

  test("test serialization") {
    val serializedBuffer = Serializer.serialize(serializerTestObject)

    assertThat(serializedBuffer.isLeft, is(true))
    assertThat(serializedBuffer.left.get.capacity(), is(equalTo(serializerTestObject.length() + Message.messageIdSize)))
  }

  test("test deserialization") {
    val serializedBuffer = Serializer.serialize(serializerTestObject)
    val deserializedObject = Message.deSerialize(serializedBuffer.left.get) getOrElse StringMessage("BAD")
    assertThat(deserializedObject.asInstanceOf[StringMessage], is(equalTo(serializerTestObject)))
  }
}

