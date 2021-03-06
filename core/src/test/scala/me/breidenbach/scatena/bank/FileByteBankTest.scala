package me.breidenbach.scatena.bank

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystems, Files, Path}

import me.breidenbach.BaseTest
import me.breidenbach.scatena.messages.{Serializer, StringMessage}
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

  var testSubject: FileByteBank = _

  override def beforeEach(): Unit = {
    testMessageOne.rewind()
    testMessageTwo.rewind()
    testSubject = new FileByteBank(filename)
  }

  override def afterAll(): Unit = {
    deleteFiles()
  }

  test("create file") {
    assertThat("assert file is available", Files.exists(filePath), is(true))
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
    val resultOneBytes = Array.ofDim[Byte](resultOne.rewind().remaining())
    val resultOneMessage = {
      resultOne.get(resultOneBytes)
      new String(resultOneBytes)
    }
    val resultTwo = testSubject.get(messageTwoPosition)
    val resultTwoSize = resultTwo.remaining()
    val sizeAfterGettingResultTwo = testSubject.size()
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
    testSubject = new FileByteBank(filename, 1024, 20)
    val messageOneSize = testMessageOne.remaining()
    val messageTwoSize = testMessageTwo.remaining()
    val messageOnePosition = testSubject.add(testMessageOne)
    val messageTwoPosition = testSubject.add(testMessageTwo)
    val sizeAfterAdds = testSubject.size()
    val resultOne = testSubject.get(messageOnePosition)
    val resultOneSize = resultOne.remaining()
    val sizeAfterGettingResultOne = testSubject.size()
    val resultOneBytes = Array.ofDim[Byte](resultOne.rewind().remaining())
    val resultOneMessage = {
      resultOne.get(resultOneBytes)
      new String(resultOneBytes)
    }
    val resultTwo = testSubject.get(messageTwoPosition)
    val resultTwoSize = resultTwo.remaining()
    val sizeAfterGettingResultTwo = testSubject.size()
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
    val position = testSubject.size()
    val serializedObject = Serializer.serialize(stringMessage).get
    val messageSize = serializedObject.remaining()
    val messagePosition = testSubject.add(serializedObject)

    assertThat("returned position is correct", messagePosition, is(equalTo(position)))
    assertThat("new position is correct", testSubject.size(), is(equalTo(position + messageSize + shortSize)))
  }

  test("fail to open") {
    assertThrows[ByteBankException] {
      testSubject = new FileByteBank(badFileName)
    }
  }
}

object FileByteBankTest {
  val sender = "Test"
  val filename = "TestBank"
  val testTextOne = "This is first test message"
  val testTextTwo = "This is second test message"
  val badFileName = "BAD:/FILENAME"
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

  def deleteFiles(): Unit = {
    Files.deleteIfExists(filePath)
  }
}