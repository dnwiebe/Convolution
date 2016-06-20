package services

import models.ConvolutionBoard
import play.api.libs.json._

/**
  * Created by dnwiebe on 6/18/16.
  */
class BoardMessageTest extends org.scalatest.path.FunSpec with BoardMessage {
  describe ("A simple ConvolutionBoard") {
    val board = ConvolutionBoard (2)
      .fixedCoordinate (1)
      .horizontalIsNext (Some (false))
      .horizontalIsWinner (None)
      .horizontalScore (2)
      .isGameOver (false)
      .verticalScore (0)
      .valueAt (0, 0, Some (1))
      .valueAt (1, 1, None)
      .valueAt (0, 1, Some (3))
      .valueAt (1, 1, Some (4))
      .build

    describe ("converted to a JsValue") {
      val result = toJsValue (board)

      it ("has the appropriate structure") {
        assert (result.toString === JsObject (Seq (
          "order" -> JsNumber (2),
          "horizontalScore" -> JsNumber (2),
          "verticalScore" -> JsNumber (0),
          "horizontalIsWinner" -> JsNull,
          "horizontalIsNext" -> JsBoolean (false),
          "isGameOver" -> JsBoolean (false),
          "fixedCoordinate" -> JsNumber (1),
          "data" -> JsArray (Seq (
            JsArray (Seq (JsNumber (1), JsNull)),
            JsArray (Seq (JsNumber (3), JsNumber (4)))
          ))
        )).toString)
      }
    }
  }
  describe ("A different ConvolutionBoard") {
    val board = ConvolutionBoard (3)
      .fixedCoordinate (2)
      .horizontalIsNext (None)
      .horizontalIsWinner (Some (true))
      .horizontalScore (3)
      .isGameOver (true)
      .verticalScore (47)
      .valueAt (0, 0, Some (1))
      .valueAt (1, 0, Some (2))
      .valueAt (2, 0, None)
      .valueAt (0, 1, Some (4))
      .valueAt (1, 1, Some (5))
      .valueAt (2, 1, Some (6))
      .valueAt (0, 2, Some (7))
      .valueAt (1, 2, Some (8))
      .valueAt (2, 2, Some (9))
      .build

    describe ("converted to a JsValue") {
      val result = toJsValue (board)

      it ("also has the appropriate structure") {
        assert (result.toString === JsObject (Seq (
          "order" -> JsNumber (3),
          "horizontalScore" -> JsNumber (3),
          "verticalScore" -> JsNumber (47),
          "horizontalIsWinner" -> JsBoolean (true),
          "horizontalIsNext" -> JsNull,
          "isGameOver" -> JsBoolean (true),
          "fixedCoordinate" -> JsNumber (2),
          "data" -> JsArray (Seq (
            JsArray (Seq (JsNumber (1), JsNumber (2), JsNull)),
            JsArray (Seq (JsNumber (4), JsNumber (5), JsNumber (6))),
            JsArray (Seq (JsNumber (7), JsNumber (8), JsNumber (9)))
          ))
        )).toString)
      }
    }
  }
}
