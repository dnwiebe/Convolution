package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import play.api.libs.json._

import scala.util.{Failure, Success}

/**
  * Created by dnwiebe on 6/5/16.
  */

object Representative {
  def apply (out: ActorRef, vestibuleService: VestibuleService, gameService: GameService)
            (implicit system: ActorSystem): ActorRef = {
    system.actorOf (props (out, vestibuleService, gameService))
  }

  def props (out: ActorRef, vestibuleService: VestibuleService, gameService: GameService) = {
    Props (classOf[Representative], out, vestibuleService, gameService)
  }
}

class Representative (out: ActorRef, vestibuleService: VestibuleService, gameService: GameService) extends Actor {

  def receive = {
    case json: String => handleIncomingMessage (json)
    case outgoingMessage: Outgoing => handleOutgoingMessage (outgoingMessage)
    case _ => throw new UnsupportedOperationException ("Test-drive me")
  }

  private def handleIncomingMessage (json: String): Unit = {
println (s"Incoming message: $json")
    val jsValue = Json.parse (json)
    val message = Incoming (jsValue)
    message match {
      case Failure (e) => throw e
      case Success (EnterVestibule (name)) => handleEnterVestibule (name)
      case Success (RejectGame (playerId)) => handleRejectGame (playerId)
    }
  }

  private def handleOutgoingMessage (message: Outgoing): Unit = {
    val json = message.toJson
println (s"Outgoing message: $json")
    out ! json
  }

  private def handleEnterVestibule (name: String): Unit = {
    vestibuleService.playerEntered (name, self)
  }

  private def handleRejectGame (playerId: Int): Unit = {
    gameService.gameReject (playerId)
  }
}
