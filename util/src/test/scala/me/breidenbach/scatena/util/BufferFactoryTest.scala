package me.breidenbach.scatena.util

import me.breidenbach.BaseTest
import me.breidenbach.scatena.util.DataConstants.udpMaxPayload
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
    val bufferThree = BufferFactory.createBuffer()

    assertThat(bufferOne.capacity(), is(equalTo(234)))
    assertThat(bufferTwo.capacity(), is(equalTo(321)))
    assertThat(bufferThree.capacity(), is(equalTo(udpMaxPayload.asInstanceOf[Int])))
    assertThat(bufferOne, is(not(equalTo(bufferTwo))))
    assertThat(bufferOne, is(not(equalTo(bufferThree))))
  }
}
