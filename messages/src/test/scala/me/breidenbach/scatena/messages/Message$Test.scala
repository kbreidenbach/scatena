package me.breidenbach.scatena.messages

import me.breidenbach.BaseTest

import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach 
  *         Date: 10/9/16.
  */
class Message$Test extends BaseTest {

  test("short conversion") {
    val expected: Short = 32134
    val bytes = Message.toByteArray(expected)

    assertThat(Message.toShort(bytes), is(equalTo(expected)))
  }

  test("int conversion") {
    val expected: Int = 2738292
    val bytes = Message.toByteArray(expected)

    assertThat(Message.toInt(bytes), is(equalTo(expected)))
  }

  test("long conversion") {
    val expected: Long = 2738292
    val bytes = Message.toByteArray(expected)

    assertThat(Message.toLong(bytes), is(equalTo(expected)))
  }

  test("double long conversion") {
    val frontLong = 12347289L
    val endLong = 927381277L
    val bytes = Message.toByteArray(frontLong, endLong)

    assertThat(Message.toDoubleLong(bytes)._1, is(equalTo(frontLong)))
    assertThat(Message.toDoubleLong(bytes)._2, is(equalTo(endLong)))
  }
}
