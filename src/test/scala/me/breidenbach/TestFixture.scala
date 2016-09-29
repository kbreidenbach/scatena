package me.breidenbach

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach, FunSuite}
import org.scalatest.mockito.MockitoSugar

import scala.language.implicitConversions

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
trait TestFixture extends FunSuite with AnswerSugar with MockitoSugar with BeforeAndAfter with BeforeAndAfterEach

trait AnswerSugar {
  implicit def toAnswer[T](f: () => T): Answer[T] = () => f()

  implicit def toAnswerWithArguments[T](f: (InvocationOnMock) => T): Answer[T] = (invocation: InvocationOnMock) => f(invocation)
}