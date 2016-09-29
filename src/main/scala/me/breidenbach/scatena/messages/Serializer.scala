package me.breidenbach.scatena.messages

import java.io.{ByteArrayOutputStream, IOException, ObjectOutputStream}
import java.nio.ByteBuffer

import scala.util.control.Exception.ignoring

/**
  * @author kbreidenbach 
  *         Date: 9/28/16.
  */
object Serializer {
  def serialize[T <: Message](serializable: T): Either[ByteBuffer, Error] = {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)

    try {
      Left(serializable.serialize())
    } catch {
      case e: IOException => Right(new Error("unable to serialize object", e))
    } finally {
      close(objectOutputStream)
      close(byteArrayOutputStream)
    }
  }

  private def close(closable: AutoCloseable): Unit = {
    ignoring(classOf[Throwable]) {
      closable.close()
    }
  }
}
