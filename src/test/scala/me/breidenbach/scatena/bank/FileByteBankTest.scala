package me.breidenbach.scatena.bank

import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystems, Files}

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.{BufferFactory, DataConstants}
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author kbreidenbach 
  *         Date: 10/1/16.
  */
class FileByteBankTest extends BaseTest {
  import FileByteBankTest._

  var subject: FileByteBank = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    testMessageOne.rewind()
    testMessageTwo.rewind()
    subject = new FileByteBank(filename)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    deleteFiles()
  }

  test("create file") {
    assertThat("assert file is available", Files.exists(filePath), is(true))
  }

  test("store message") {
    val position = subject.size()
    val (messagePosition, messageSize) = subject.add(testMessageOne)
    assertThat("returned position is correct", messagePosition, is(equalTo(position)))
    assertThat("new position is correct", subject.size(),
      is(equalTo(position + messageOneLength + DataConstants.shortSize)))
    assertThat("message size is equal to bank size", messageSize, is(equalTo(messageOneLength)))
  }

  test("store multiple messages") {
    val startingPosition = subject.size()
    val (messageOnePosition, messageOneSize) = subject.add(testMessageOne.slice())
    val (messageTwoPosition, messageTwoSize) = subject.add(testMessageTwo)

    assertThat("returned position after first is correct", messageOnePosition, is(equalTo(startingPosition)))
    assertThat("returned position after second is correct", messageTwoPosition,
      is(equalTo(startingPosition + messageOneSize + DataConstants.shortSize)))
    assertThat("new position is correct", subject.size(),
      is(equalTo(startingPosition + messageOneSize + DataConstants.shortSize +
        messageTwoSize + DataConstants.shortSize)))
  }

  test("retrieving messages") {
    val (messageOnePosition, messageOneSize) = subject.add(testMessageOne)
    val (messageTwoPosition, messageTwoSize) = subject.add(testMessageTwo)
    val sizeAfterAdds = subject.size()
    val (resultOne, resultOneSize) = subject.get(messageOnePosition)
    val sizeAfterGettingResultOne = subject.size()
    val resultOneBytes = Array.ofDim[Byte](resultOne.rewind().remaining())
    val resultOneMessage = {
      resultOne.get(resultOneBytes)
      new String(resultOneBytes)
    }
    val (resultTwo, resultTwoSize) = subject.get(messageTwoPosition)
    val sizeAfterGettingResultTwo = subject.size()
    val resultTwoBytes = Array.ofDim[Byte](resultTwo.rewind().remaining())
    val resultTwoMessage = {
      resultTwo.get(resultTwoBytes)
      new String(resultTwoBytes)
    }

    assertThat("check initial size against sized after get",
      sizeAfterAdds == sizeAfterGettingResultOne && sizeAfterAdds == sizeAfterGettingResultTwo, is(true))
    assertThat("message one size is correct", messageOneSize, is(equalTo(messageOneLength)))
    assertThat("check first returned message size is same as message one size ", resultOneSize,
      is(equalTo(messageOneLength)))
    assertThat("check first returned message text", resultOneMessage, is(equalTo(testTextOne)))
    assertThat("message two size is correct", messageTwoSize, is(equalTo(messageTwoLength)))
    assertThat("check second returned message size is same as message one size ", resultTwoSize,
      is(equalTo(messageTwoLength)))
    assertThat("check second returned message text", resultTwoMessage, is(equalTo(testTextTwo)))
  }

  test("fail to open") {
    assertThrows[ByteBankException] {
      subject = new FileByteBank(badFileName)
    }
  }
}


object FileByteBankTest {

  private[FileByteBankTest] val filename = "TestBank"
  private[FileByteBankTest] val filePath = FileSystems.getDefault.getPath(filename)
  private[FileByteBankTest] val testTextOne = "This is first test message"
  private[FileByteBankTest] val testTextTwo = "This is second test message"
  private[FileByteBankTest] val messageOneLength = testTextOne.length.asInstanceOf[Short]
  private[FileByteBankTest] val messageTwoLength = testTextTwo.length.asInstanceOf[Short]
  private[FileByteBankTest] val testMessageOne = BufferFactory.createBuffer(messageOneLength)
  private[FileByteBankTest] val testMessageTwo = BufferFactory.createBuffer(messageTwoLength)
  private[FileByteBankTest] val badFileName = "BAD:/FILENAME"

  testMessageOne.put(testTextOne.getBytes(StandardCharsets.UTF_8))
  testMessageOne.flip()

  testMessageTwo.put(testTextTwo.getBytes(StandardCharsets.UTF_8))
  testMessageTwo.flip()

  def deleteFiles(): Unit = {
    Files.deleteIfExists(filePath)
  }

}
