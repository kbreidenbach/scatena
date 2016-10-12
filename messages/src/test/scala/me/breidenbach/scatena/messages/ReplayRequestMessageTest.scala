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
    assertThat(serializedObject.isLeft, is(true))
    assertThat(serializedObject.left.get.remaining(),
      is(equalTo(DataConstants.longSize.asInstanceOf[Int] + Message.messageIdSize)))
  }

  test ("deserialize") {
    val serializedObject = Serializer.serialize(replayRequestMessage)
    val deserializedObject = Message.deSerialize(serializedObject.left.get) getOrElse ReplayRequestMessage(0, 0)
    assertThat(deserializedObject.asInstanceOf[ReplayRequestMessage], is(equalTo(replayRequestMessage)))
  }
}

object ReplayRequestMessageTest {
  val startSequence = 1234
  val endSequence = 63353
  val replayRequestMessage = ReplayRequestMessage(startSequence, endSequence)
}
