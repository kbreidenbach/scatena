package me.breidenbach.scatena.bank

import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystems, Files}

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.{BufferFactory, DataConstants}
import me.breidenbach.scatena.util.DataConstants._
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
    subject = new FileByteBank(filename)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    deleteFiles()
  }

  test("create file") {
    assertThat("assert file is available", Files.exists(filePath), is(true))
  }

  test("store message") {
    val position = subject.size()
    assertThat("returned position is correct", subject.add(testMessageOne), is(equalTo(position)))
    assertThat("new position is correct", subject.size(), is(equalTo(position + messageOneLength)))
  }

  test("store multiple messages") {
    val startingPosition = subject.size()
    val positionOne = subject.add(testMessageOne.slice())
    val positionTwo = subject.add(testMessageTwo)

    assertThat("returned position after first is correct", positionOne, is(equalTo(startingPosition)))
    assertThat("returned position after second is correct", positionTwo,
      is(equalTo(startingPosition + messageOneLength)))
    assertThat("new position is correct", subject.size(),
      is(equalTo(startingPosition + messageOneLength + messageTwoLength)))
  }

}

object FileByteBankTest {

  private[FileByteBankTest] val filename = "TestBank"
  private[FileByteBankTest] val filePath = FileSystems.getDefault.getPath(filename)
  private[FileByteBankTest] val testTextOne = "This is first test message"
  private[FileByteBankTest] val testTextTwo = "This is second test message"
  private[FileByteBankTest] val messageOneLength = DataConstants.shortSize + testTextOne.length
  private[FileByteBankTest] val messageTwoLength = DataConstants.shortSize + testTextTwo.length
  private[FileByteBankTest] val testMessageOne = BufferFactory.createBuffer(messageOneLength)
  private[FileByteBankTest] val testMessageTwo = BufferFactory.createBuffer(messageTwoLength)

  testMessageOne.putShort(DataConstants.shortSize.asInstanceOf[Short])
  testMessageOne.put(testTextOne.getBytes(StandardCharsets.UTF_8))
  testMessageOne.flip()

  testMessageTwo.putShort(DataConstants.shortSize.asInstanceOf[Short])
  testMessageTwo.put(testTextTwo.getBytes(StandardCharsets.UTF_8))
  testMessageTwo.flip()

  def deleteFiles(): Unit = {
    Files.deleteIfExists(filePath)
  }
}
