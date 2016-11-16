package me.breidenbach.scatena.messages

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.DataConstants
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach 
  *         Date: 10/11/16.
  */
class ReplayRequestMessageTest extends BaseTest {
  import ReplayRequestMessageTest._

  test ("serialization") {
    val serializedObject = Serializer.serialize(replayRequestMessage)
    assertThat(serializedObject.isSuccess, is(true))
    assertThat(serializedObject.get.remaining(),
      is(equalTo(DataConstants.longSize.asInstanceOf[Int] * 2 + Message.messageIdSize +
        MessageConstants.senderNameSize)))
  }

  test ("deserialize") {
    val serializedObject = Serializer.serialize(replayRequestMessage)
    val deserializedObject = Message.deSerialize(serializedObject.get) getOrElse ReplayRequestMessage(sender, 0, 0)
    assertThat(deserializedObject.asInstanceOf[ReplayRequestMessage], is(equalTo(replayRequestMessage)))
    assertThat(deserializedObject.asInstanceOf[ReplayRequestMessage].startSequence, is(equalTo(startSequence)))
    assertThat(deserializedObject.asInstanceOf[ReplayRequestMessage].endSequence, is(equalTo(endSequence)))
  }
}

object ReplayRequestMessageTest {
  val sender = "test"
  val startSequence = 1234L
  val endSequence = 63353L
  val replayRequestMessage = ReplayRequestMessage(sender, startSequence, endSequence)
}
