package me.breidenbach.scatena.messages

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.DataConstants
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach 
  *         Date: 10/11/16.
  */
class SequenceUnavailableMessageTest extends BaseTest {
  import SequenceUnavailableMessageTest._

  test ("serialization") {
    val serializedObject = Serializer.serialize(sequenceUnavailableMessage)
    assertThat(serializedObject.isSuccess, is(true))
    assertThat(serializedObject.get.remaining(),
      is(equalTo(DataConstants.longSize.asInstanceOf[Int] * 2 + Message.messageIdSize)))
  }

  test ("deserialize") {
    val serializedObject = Serializer.serialize(sequenceUnavailableMessage)
    val deserializedObject = Message.deSerialize(serializedObject.get) getOrElse ReplayRequestMessage(0, 0)
    assertThat(deserializedObject.asInstanceOf[SequenceUnavailableMessage], is(equalTo(sequenceUnavailableMessage)))
  }
}

object SequenceUnavailableMessageTest {
  val startSequence = 233567
  val endSequence = 7612672
  val sequenceUnavailableMessage = SequenceUnavailableMessage(startSequence, endSequence)
}
