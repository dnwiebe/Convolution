package services

import models.ConvolutionBoard
import play.api.libs.json.{JsArray, JsNull, _}

/**
  * Created by dnwiebe on 6/18/16.
  */
trait BoardMessage {
  protected def toJsValue (b: ConvolutionBoard): JsValue = {
    JsObject (Seq (
      "order" -> JsNumber (b.order),
      "horizontalScore" -> JsNumber (b.horizontalScore),
      "verticalScore" -> JsNumber (b.verticalScore),
      "horizontalIsWinner" -> (if (b.horizontalIsWinner.isEmpty) JsNull else JsBoolean (b.horizontalIsWinner.get)),
      "horizontalIsNext" -> (if (b.horizontalIsNext.isEmpty) JsNull else JsBoolean (b.horizontalIsNext.get)),
      "isGameOver" -> JsBoolean (b.isGameOver),
      "fixedCoordinate" -> JsNumber (b.fixedCoordinate),
      "data" -> JsArray (for (rowIdx <- 0 until b.order) yield {
        JsArray (for (colIdx <- 0 until b.order) yield {
          b.valueAt (colIdx, rowIdx) match {
            case None => JsNull
            case Some (number) => JsNumber (number)
          }
        })
      })
    ))
  }
}
