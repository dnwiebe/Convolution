package services

import play.api.libs.json.JsValue

import scala.util.{Failure, Success, Try}

/**
  * Created by dnwiebe on 6/10/16.
  */

object Incoming {
  private val TYPE_MAP = Map (
    "enterVestibule" -> classOf[EnterVestibule],
    "rejectGame" -> classOf[RejectGame]
  )

  def apply[T <: Incoming] (cls: Class[T], jsValue: JsValue): Try[T] = {
    apply (jsValue) match {
      case Failure (e) => Failure (e)
      case Success (x) => Success (x.asInstanceOf[T])
    }
  }

  def apply (jsValue: JsValue): Try[Incoming] = {
    try {
      val opcode = (jsValue \ "type").as[String]
      val cls = TYPE_MAP(opcode)
      val data = (jsValue \ "data").as[JsValue]
      val ctor = cls.getConstructor (classOf[JsValue])
      val msg = ctor.newInstance (data).asInstanceOf[Incoming]
      Success (msg)
    }
    catch {
      case e: Exception => Failure (e)
    }
  }
}

class Incoming (opcode: String) {

}
