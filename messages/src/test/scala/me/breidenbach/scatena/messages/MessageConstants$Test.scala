package me.breidenbach.scatena.messages

import me.breidenbach.BaseTest
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach
  * Date: 10/3/16.
  */
class MessageConstants$Test extends BaseTest {
  import MessageConstants._

  test("bit set") {
    val byte: Byte = "10011011".b.asInstanceOf[Byte]

    assertThat(bitSet(byte, 0), is(true))
    assertThat(bitSet(byte, 1), is(true))
    assertThat(bitSet(byte, 2), is(false))
    assertThat(bitSet(byte, 3), is(true))
    assertThat(bitSet(byte, 4), is(true))
    assertThat(bitSet(byte, 5), is(false))
    assertThat(bitSet(byte, 6), is(false))
    assertThat(bitSet(byte, 7), is(true))
  }

  test("set bit") {
    val byte = setBit(setBit(setBit(0, 5), 2), 6)

    assertThat(bitSet(byte, 0), is(false))
    assertThat(bitSet(byte, 1), is(false))
    assertThat(bitSet(byte, 2), is(true))
    assertThat(bitSet(byte, 3), is(false))
    assertThat(bitSet(byte, 4), is(false))
    assertThat(bitSet(byte, 5), is(true))
    assertThat(bitSet(byte, 6), is(true))
    assertThat(bitSet(byte, 7), is(false))
  }
}


