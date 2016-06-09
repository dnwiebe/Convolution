package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by dnwiebe on 6/5/16.
  */

object VestibuleActor {

  def apply (out: ActorRef)(implicit system: ActorSystem): ActorRef = {
    system.actorOf (props (out))
  }

  def props (out: ActorRef) = Props (classOf[VestibuleActor], out)
}

class VestibuleActor (out: ActorRef) extends Actor {

  def receive = {
    case _ =>
  }
}
