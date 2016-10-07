package me.breidenbach.scatena.bank

import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystems, Files}

import me.breidenbach.BaseTest
import me.breidenbach.scatena.messages.StringMessage
import me.breidenbach.scatena.util.BufferFactory
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach
  *         Date: 10/1/16.
  */
class FileByteBankTest extends BaseTest {
  import me.breidenbach.scatena.util.DataConstants._
  import FileByteBankTest._

  var subject: FileByteBank = _

  override def beforeEach(): Unit = {
    testMessageOne.rewind()
    testMessageTwo.rewind()
    subject = new FileByteBank(filename)
  }

  override def afterAll(): Unit = {
    deleteFiles()
  }

  test("create file") {
    assertThat("assert file is available", Files.exists(filePath), is(true))
  }

  test("store message") {
    val position = subject.size()
    val messageSize = testMessageOne.remaining()
    val messagePosition = subject.add(testMessageOne)
    assertThat("returned position is correct", messagePosition, is(equalTo(position)))
    assertThat("new position is correct", subject.size(),
      is(equalTo(position + messageOneLength + shortSize)))
    assertThat("message size is equal to bank size", messageSize, is(equalTo(messageOneLength)))
  }

  test("store multiple messages") {
    val startingPosition = subject.size()
    val messageOneSize = testMessageOne.remaining()
    val messageTwoSize = testMessageTwo.remaining()
    val messageOnePosition = subject.add(testMessageOne)
    val messageTwoPosition = subject.add(testMessageTwo)

    assertThat("returned position after first is correct", messageOnePosition, is(equalTo(startingPosition)))
    assertThat("returned position after second is correct", messageTwoPosition,
      is(equalTo(startingPosition + messageOneSize + shortSize)))
    assertThat("new position is correct", subject.size(),
      is(equalTo(startingPosition + messageOneSize + shortSize +
        messageTwoSize + shortSize)))
  }

  test("retrieving messages") {
    val messageOneSize = testMessageOne.remaining()
    val messageTwoSize = testMessageTwo.remaining()
    val messageOnePosition = subject.add(testMessageOne)
    val messageTwoPosition = subject.add(testMessageTwo)
    val sizeAfterAdds = subject.size()
    val resultOne = subject.get(messageOnePosition)
    val resultOneSize = resultOne.remaining()
    val sizeAfterGettingResultOne = subject.size()
    val resultOneBytes = Array.ofDim[Byte](resultOne.rewind().remaining())
    val resultOneMessage = {
      resultOne.get(resultOneBytes)
      new String(resultOneBytes)
    }
    val resultTwo = subject.get(messageTwoPosition)
    val resultTwoSize = resultTwo.remaining()
    val sizeAfterGettingResultTwo = subject.size()
    val resultTwoBytes = Array.ofDim[Byte](resultTwo.rewind().remaining())
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

  test("retrieving messages from bank with small memory buffer flush size") {
    deleteFiles()
    subject = new FileByteBank(filename, 1024, 20)
    val messageOneSize = testMessageOne.remaining()
    val messageTwoSize = testMessageTwo.remaining()
    val messageOnePosition = subject.add(testMessageOne)
    val messageTwoPosition = subject.add(testMessageTwo)
    val sizeAfterAdds = subject.size()
    val resultOne = subject.get(messageOnePosition)
    val resultOneSize = resultOne.remaining()
    val sizeAfterGettingResultOne = subject.size()
    val resultOneBytes = Array.ofDim[Byte](resultOne.rewind().remaining())
    val resultOneMessage = {
      resultOne.get(resultOneBytes)
      new String(resultOneBytes)
    }
    val resultTwo = subject.get(messageTwoPosition)
    val resultTwoSize = resultTwo.remaining()
    val sizeAfterGettingResultTwo = subject.size()
    val resultTwoBytes = Array.ofDim[Byte](resultTwo.rewind().remaining())
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

  test("add a message type")
  {
    val position = subject.size()
    val serializedObject = stringMessage.serialize()
    val messageSize = serializedObject.remaining()
    val messagePosition = subject.add(serializedObject)

    assertThat("returned position is correct", messagePosition, is(equalTo(position)))
    assertThat("new position is correct", subject.size(), is(equalTo(position + messageSize + shortSize)))
  }

  test("fail to open") {
    assertThrows[ByteBankException] {
      subject = new FileByteBank(badFileName)
    }
  }
}

object FileByteBankTest {
  val filename = "TestBank"
  val filePath = FileSystems.getDefault.getPath(filename)
  val testTextOne = "This is first test message"
  val testTextTwo = "This is second test message"
  val messageOneLength = testTextOne.length
  val messageTwoLength = testTextTwo.length
  val testMessageOne = BufferFactory.createBuffer(messageOneLength)
  val testMessageTwo = BufferFactory.createBuffer(messageTwoLength)
  val badFileName = "BAD:/FILENAME"
  val stringMessage = StringMessage(testTextOne)

  testMessageOne.put(testTextOne.getBytes(StandardCharsets.UTF_8))
  testMessageOne.flip()

  testMessageTwo.put(testTextTwo.getBytes(StandardCharsets.UTF_8))
  testMessageTwo.flip()

  def deleteFiles(): Unit = {
    Files.deleteIfExists(filePath)
  }
}