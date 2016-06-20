package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import play.api.libs.json._

/**
  * Created by dnwiebe on 6/5/16.
  */

object Representative {
  def apply (out: ActorRef, vestibuleService: VestibuleService)(implicit system: ActorSystem): ActorRef = {
    system.actorOf (props (out, vestibuleService))
  }

  def props (out: ActorRef, vestibuleService: VestibuleService) = {
    Props (classOf[Representative], out, vestibuleService)
  }
}

class Representative (out: ActorRef, vestibuleService: VestibuleService) extends Actor {

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
      case None => throw new UnsupportedOperationException ("Test-drive me")
      case Some (EnterVestibule (name)) => handleEnterVestibule (name)
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
}
