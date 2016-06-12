package services

import akka.actor.ActorRef

/**
  * Created by dnwiebe on 6/12/16.
  */

case class DisplayPlayer (
  id: Int,
  name: String,
  timestamp: Long,
  representative: ActorRef
)
