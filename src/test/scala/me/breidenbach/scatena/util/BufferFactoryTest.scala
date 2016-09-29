package me.breidenbach.scatena.util

import me.breidenbach.TestFixture
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers._

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
class BufferFactoryTest extends TestFixture {
  test ("test create buffer") {
    val bufferOne = BufferFactory.createBuffer(234)
    val bufferTwo = BufferFactory.createBuffer(321)

    assertThat(bufferOne.capacity(), is(equalTo(234)))
    assertThat(bufferTwo.capacity(), is(equalTo(321)))
    assertThat(bufferOne, is(not(equalTo(bufferTwo))))
  }
}
