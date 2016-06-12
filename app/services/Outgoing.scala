package services

import play.api.libs.json._

/**
  * Created by dnwiebe on 6/10/16.
  */
abstract class Outgoing (opcode: String) {
  def toJson: String = {
    val jsValue = JsObject (Seq ("opcode" -> JsString (opcode), "data" -> toJsValue))
    jsValue.toString ()
  }

  protected def toJsValue: JsValue
}
