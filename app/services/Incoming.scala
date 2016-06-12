package services

import play.api.libs.json.JsValue

/**
  * Created by dnwiebe on 6/10/16.
  */

object Incoming {
  private val TYPE_MAP = Map (
    "enterVestibule" -> classOf[EnterVestibule]
  )

  def apply[T <: Incoming] (cls: Class[T], jsValue: JsValue): Option[T] = {
    apply (jsValue) match {
      case None => None
      case Some (x) => Some (x.asInstanceOf[T])
    }
  }

  def apply (jsValue: JsValue): Option[Incoming] = {
    try {
      val opcode = (jsValue \ "opcode").as[String]
      val cls = TYPE_MAP(opcode)
      val data = (jsValue \ "data").as[JsValue]
      val ctor = cls.getConstructor (classOf[JsValue])
      val msg = ctor.newInstance (data).asInstanceOf[Incoming]
      Some (msg)
    }
    catch {
      case e: Exception => None
    }
  }
}

class Incoming (opcode: String) {

}
