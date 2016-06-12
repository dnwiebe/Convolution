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

  def props (out: ActorRef, vestibuleService: VestibuleService) = Props (classOf[Representative], out, vestibuleService)
}

class Representative (out: ActorRef, vestibuleService: VestibuleService) extends Actor {

  def receive = {
    case json: String => handleIncomingMessage (json)
    case _ => throw new UnsupportedOperationException ("Test-drive me")
  }

  private def handleIncomingMessage (json: String): Unit = {
    val jsValue = Json.parse (json)
    val message = Incoming (jsValue)
    message match {
      case None => throw new UnsupportedOperationException ("Test-drive me")
      case Some (EnterVestibule (name)) => handleEnterVestibule (name)
    }
  }

  private def handleEnterVestibule (name: String): Unit = {
    vestibuleService.playerEntered (name, self)
  }

  private def sendMessage (message: Outgoing): Unit = {
    val string = message.toJson
    out ! string
  }
}
