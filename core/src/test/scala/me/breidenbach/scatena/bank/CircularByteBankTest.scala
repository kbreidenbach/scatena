package me.breidenbach.scatena.bank

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystems, Path}

import me.breidenbach.BaseTest
import me.breidenbach.scatena.messages.StringMessage
import me.breidenbach.scatena.util.BufferFactory
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach
  *         Date: 10/7/16
  */
class CircularByteBankTest extends BaseTest {
  import me.breidenbach.scatena.util.DataConstants._
  import CircularByteBankTest._

  var testSubject: CircularByteBank = _

  override def beforeEach(): Unit = {
    testMessageOne.rewind()
    testMessageTwo.rewind()
    testSubject = new CircularByteBank()
  }

  test("store message") {
    val position = testSubject.size()
    val messageSize = testMessageOne.remaining()
    val messagePosition = testSubject.add(testMessageOne)
    assertThat("returned position is correct", messagePosition, is(equalTo(position)))
    assertThat("new position is correct", testSubject.size(),
      is(equalTo(position + messageOneLength + shortSize)))
    assertThat("message size is equal to bank size", messageSize, is(equalTo(messageOneLength)))
  }

  test("store multiple messages") {
    val startingPosition = testSubject.size()
    val messageOneSize = testMessageOne.remaining()
    val messageTwoSize = testMessageTwo.remaining()
    val messageOnePosition = testSubject.add(testMessageOne)
    val messageTwoPosition = testSubject.add(testMessageTwo)

    assertThat("returned position after first is correct", messageOnePosition, is(equalTo(startingPosition)))
    assertThat("returned position after second is correct", messageTwoPosition,
      is(equalTo(startingPosition + messageOneSize + shortSize)))
    assertThat("new position is correct", testSubject.size(),
      is(equalTo(startingPosition + messageOneSize + shortSize +
        messageTwoSize + shortSize)))
  }

  test("retrieving messages") {
    val messageOneSize = testMessageOne.remaining()
    val messageTwoSize = testMessageTwo.remaining()
    val messageOnePosition = testSubject.add(testMessageOne)
    val messageTwoPosition = testSubject.add(testMessageTwo)
    val sizeAfterAdds = testSubject.size()
    val resultOne = testSubject.get(messageOnePosition)
    val resultOneSize = resultOne.remaining()
    val sizeAfterGettingResultOne = testSubject.size()
    val resultOneBytes = Array.ofDim[Byte](resultOneSize)
    val resultOneMessage = {
      resultOne.get(resultOneBytes)
      new String(resultOneBytes)
    }
    val resultTwo = testSubject.get(messageTwoPosition)
    val resultTwoSize = resultTwo.remaining()
    val sizeAfterGettingResultTwo = testSubject.size()
    val resultTwoBytes = Array.ofDim[Byte](resultTwoSize)
    val resultTwoMessage = {
      resultTwo.get(resultTwoBytes)
      new String(resultTwoBytes)
    }

    assertThat("check initial size against sized after get",
      sizeAfterAdds == sizeAfterGettingResultOne && sizeAfterAdds == sizeAfterGettingResultTwo, is(true))
    assertThat("message one size is correct", messageOneSize, is(equalTo(messageOneLength)))
    assertThat("check first returned message size is correct", resultOneSize, is(equalTo(messageOneLength)))
    assertThat("check first returned message text", resultOneMessage, is(equalTo(testTextOne)))
    assertThat("message two size is correct", messageTwoSize, is(equalTo(messageTwoLength)))
    assertThat("check second returned message size is correct", resultTwoSize, is(equalTo(messageTwoLength)))
    assertThat("check second returned message text", resultTwoMessage, is(equalTo(testTextTwo)))
  }

  test("retrieve messages from a smaller circular buffer") {
    testSubject = new CircularByteBank(CircularByteBank.minimumSize)
    val storedSize = testMessageOne.remaining() + shortSize
    val maxMessages: Int = CircularByteBank.minimumSize / storedSize // messages after this will be looped in buffer
    val positionOne = {
      fillBuffer(testMessageOne, maxMessages)
      testSubject.add(testMessageOne)
    }
    val positionTwo = testSubject.add(testMessageTwo)
    val resultOne = testSubject.get(positionOne)
    val resultOneBytes = Array.ofDim[Byte](resultOne.remaining())
    val resultOneMessage = {
      resultOne.get(resultOneBytes)
      new String(resultOneBytes)
    }
    val resultTwo = testSubject.get(positionTwo)
    val resultTwoBytes = Array.ofDim[Byte](resultTwo.remaining())
    val resultTwoMessage = {
      resultTwo.get(resultTwoBytes)
      new String(resultTwoBytes)
    }

    assertThat("check message one text", resultOneMessage, is(equalTo(testTextOne)))
    assertThat("check message two text", resultTwoMessage, is(equalTo(testTextTwo)))

    testMessageOne.rewind()
    testMessageTwo.rewind()

    val positionThree = {
      fillBuffer(testMessageOne, maxMessages)
      testSubject.add(testMessageOne)
    }
    val positionFour = testSubject.add(testMessageTwo)
    val shouldBeEmptyOne = testSubject.get(positionOne)
    val shouldBeEmptyTwo = testSubject.get(positionTwo)

    assertThat(shouldBeEmptyOne.limit(), is(equalTo(0)))
    assertThat(shouldBeEmptyTwo.limit(), is(equalTo(0)))

    val resultThree = testSubject.get(positionThree)
    val resultThreeBytes = Array.ofDim[Byte](resultThree.remaining())
    val resultThreeMessage = {
      resultOne.get(resultThreeBytes)
      new String(resultThreeBytes)
    }
    val resultFour = testSubject.get(positionFour)
    val resultFourBytes = Array.ofDim[Byte](resultFour.remaining())
    val resultFourMessage = {
      resultTwo.get(resultFourBytes)
      new String(resultFourBytes)
    }

    assertThat("check message one text", resultThreeMessage, is(equalTo(testTextOne)))
    assertThat("check message two text", resultFourMessage, is(equalTo(testTextTwo)))
  }

  test("ensure a bank too small can't be created") {
    assertThrows[ByteBankException] {
      testSubject = new CircularByteBank(20)
    }
  }

  private def fillBuffer(message: ByteBuffer, count: Int): Unit = {
    1 to count foreach (_ => testSubject.add {message.rewind(); message})
    message.rewind()
  }
}

object CircularByteBankTest {
  val sender = "Test"
  val filename = "TestBank"
  val badFileName = "BAD:/FILENAME"
  val testTextOne = "This is first test message"
  val testTextTwo = "This is second test message"
  val filePath: Path = FileSystems.getDefault.getPath(filename)
  val messageOneLength: Int = testTextOne.length
  val messageTwoLength: Int = testTextTwo.length
  val testMessageOne: ByteBuffer = BufferFactory.createBuffer(messageOneLength)
  val testMessageTwo: ByteBuffer = BufferFactory.createBuffer(messageTwoLength)
  val stringMessage = new StringMessage(sender, testTextOne)

  testMessageOne.put(testTextOne.getBytes(StandardCharsets.UTF_8))
  testMessageOne.flip()

  testMessageTwo.put(testTextTwo.getBytes(StandardCharsets.UTF_8))
  testMessageTwo.flip()
}