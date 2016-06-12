package services

import akka.actor.ActorRef
import com.google.inject.Singleton

import scala.collection.mutable.ListBuffer
import utils.Utils._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
  * Created by dnwiebe on 6/5/16.
  */

@Singleton
class VestibuleService {

  private var nextId = 1
  var clock: () => Long = {() => System.currentTimeMillis ()}

  private val data = ListBuffer[DisplayPlayer] ()

  def playerChallenge (meId: Int, himId: Int): Future[(Option[DisplayPlayer], Option[DisplayPlayer])] = {
    Future {
      val mePlayerOpt = data.find {p => p.id == meId}
      val himPlayerOpt = data.find {p => p.id == himId}
      if (mePlayerOpt.isDefined) {removePlayerById (mePlayerOpt.get.id)}
      (mePlayerOpt, himPlayerOpt)
    }
  }

  def waitingPlayers (): Future[List[DisplayPlayer]] = {
    Future {
      data.toList
    }
  }

  def playerEntered (name: String, representative: ActorRef): Future[Int] = {
    Future {
      val id = nextId
      data.append (DisplayPlayer (id, name, clock (), representative))
      nextId += 1
      id
    }
  }

  def playerLeft (id: Int): Unit = {
    removePlayerById (id)
  }

  private def removePlayerById (id: Int): Boolean = {
    val (index, found) = data.foldLeft ((0, false)) {(soFar, elem) =>
      soFar match {
        case (idx, true) => (idx, true)
        case (idx, false) if elem.id == id => (idx, true)
        case (idx, false) => (idx + 1, false)
      }
    }
    if (found) {
      data.remove (index)
      true
    }
    else {
      false
    }
  }
}
