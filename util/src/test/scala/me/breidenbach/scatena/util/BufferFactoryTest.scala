package me.breidenbach.scatena.util

import me.breidenbach.BaseTest
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
class BufferFactoryTest extends BaseTest {
  test ("create buffer") {
    val bufferOne = BufferFactory.createBuffer(234)
    val bufferTwo = BufferFactory.createBuffer(321)

    assertThat(bufferOne.capacity(), is(equalTo(234)))
    assertThat(bufferTwo.capacity(), is(equalTo(321)))
    assertThat(bufferOne, is(not(equalTo(bufferTwo))))
  }
}
