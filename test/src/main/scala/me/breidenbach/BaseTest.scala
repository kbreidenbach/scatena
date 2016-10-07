package me.breidenbach

import org.junit.runner.RunWith
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mockito.MockitoSugar

import scala.language.implicitConversions

/**
  * @author Kevin Breidenbach
  *         Date: 9/28/16.
  */
trait TestFixture extends FunSuite with AnswerSugar with MockitoSugar with BeforeAndAfterEach with BeforeAndAfterAll

trait AnswerSugar {
  implicit def toAnswer[T](f: () => T): Answer[T] = (_) => f()
  implicit def toAnswerWithArguments[T](f: (InvocationOnMock) => T): Answer[T] =
    (invocation: InvocationOnMock) => f(invocation)
}

@RunWith(classOf[JUnitRunner])
abstract class BaseTest extends TestFixture