package services

import play.api.libs.json._

/**
  * Created by dnwiebe on 6/12/16.
  */

case class WaitList (players: List[DisplayPlayer]) extends Outgoing ("waitList") with PlayerMessage {
  override protected def toJsValue = JsArray (players.map {toJsValue (_)})
}

case class PlayerAccepted (player: DisplayPlayer) extends Outgoing ("acceptPlayer") with PlayerMessage {  // TODO: change the type
  override protected def toJsValue = toJsValue (player)
}

case class Challenge (challenger: DisplayPlayer) extends Outgoing ("challenge") with PlayerMessage {
  override protected def toJsValue = toJsValue (challenger)
}

case class EnterVestibule (name: String) extends Incoming ("enterVestibule") {
  def this (jsValue: JsValue) {this ((jsValue \ "name").as[String])}
}

case class RejectGame (playerId: Int) extends Incoming ("rejectGame") {
  def this (jsValue: JsValue) {this ((jsValue \ "playerId").as[Int])}
}
