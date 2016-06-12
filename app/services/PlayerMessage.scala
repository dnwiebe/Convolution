package services

import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

/**
  * Created by dnwiebe on 6/12/16.
  */

trait PlayerMessage {
  protected def toJsValue (p: DisplayPlayer): JsValue = {
    JsObject (Seq ("id" -> JsNumber (p.id), "name" -> JsString (p.name), "timestamp" -> JsNumber (p.timestamp)))
  }
}
