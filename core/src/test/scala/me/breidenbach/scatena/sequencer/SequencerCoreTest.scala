package me.breidenbach.scatena.sequencer

import java.nio.file.{FileSystems, Files}

import me.breidenbach.BaseTest
import me.breidenbach.scatena.bank.{ByteBank, FileByteBank}
import me.breidenbach.scatena.messages.{MessageConstants, Serializer, StringMessage}
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach
  *         Date: 10/2/16.
  */
class SequencerCoreTest extends BaseTest {
  import SequencerCoreTest._

  var byteBank: ByteBank = new FileByteBank(filename)

  override def beforeEach(): Unit = {
    byteBank.reset()
    SequencerCore.setByteBank(sessionId, byteBank)
  }

  override def afterEach(): Unit = {
    SequencerCore.reset()
  }

  override def afterAll(): Unit = {
    val path = FileSystems.getDefault.getPath(filename)
    Files.deleteIfExists(path)
  }

  test("ensure sequencer core creates correct buffer") {

    val message = new StringMessage("Test Message")
    val buffer = SequencerCore.sequence(message)

    assertThat(buffer.isSuccess, is (true))
    assertThat(buffer.get.limit(), is(equalTo(buffer.get.remaining())))
    assertThat(buffer.get.limit(), is(equalTo(Serializer.serialize(message).get.remaining() +
      MessageConstants.messageDataPosition)))
  }
}

object SequencerCoreTest {
  private[SequencerCoreTest] val filename = "TEST_FILE"
  private[SequencerCoreTest] val sessionId = 232
}
