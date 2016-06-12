package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.google.inject.{Inject, Singleton}

import scala.collection.mutable.ListBuffer
import akka.pattern.ask

import scala.concurrent.duration._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
  * Created by dnwiebe on 6/5/16.
  */

object VestibuleService {
}

@Singleton
class VestibuleService @Inject() (implicit system: ActorSystem) {
  private implicit val timeout: akka.util.Timeout = 1 seconds
  private val actor = VestibuleActor (this)
  var clock: () => Long = {() => System.currentTimeMillis ()}

  def playerChallenge (meId: Int, himId: Int): Future[(Option[DisplayPlayer], Option[DisplayPlayer])] = {
    val response = (actor ? PlayerChallengeReq (meId, himId)).mapTo[PlayerChallengeResp]
    response.map {r => (r.mePlayerOpt, r.himPlayerOpt)}
  }

  def waitingPlayers (): Future[List[DisplayPlayer]] = {
    val response = (actor ? WaitingPlayersReq ()).mapTo[WaitingPlayersResp]
    response.map {r => r.players}
  }

  def playerEntered (name: String, representative: ActorRef): Future[Int] = {
    val response = (actor ? PlayerEnteredReq (name, representative)).mapTo[PlayerEnteredResp]
    response.map {r => r.id}
  }

  def playerLeft (id: Int): Unit = {
    actor ! PlayerLeftReq (id)
  }

  private case class PlayerChallengeReq (meId: Int, himId: Int)
  private case class PlayerChallengeResp (mePlayerOpt: Option[DisplayPlayer], himPlayerOpt: Option[DisplayPlayer])
  private case class WaitingPlayersReq ()
  private case class WaitingPlayersResp (players: List[DisplayPlayer])
  private case class PlayerEnteredReq (name: String, representative: ActorRef)
  private case class PlayerEnteredResp (id: Int)
  private case class PlayerLeftReq (id: Int)

  object VestibuleActor {
    def apply (service: VestibuleService)(implicit system: ActorSystem): ActorRef = {
      system.actorOf (Props (classOf[VestibuleActor], service))
    }
  }

  class VestibuleActor () extends Actor {

    private var nextId = 1

    private val data = ListBuffer[DisplayPlayer] ()

    def receive = {
      case msg: PlayerChallengeReq => playerChallenge (msg.meId, msg.himId)
      case msg: WaitingPlayersReq => waitingPlayers ()
      case msg: PlayerEnteredReq => playerEntered (msg.name, msg.representative)
      case msg: PlayerLeftReq => playerLeft (msg.id)
    }

    private def playerChallenge (meId: Int, himId: Int): Unit = {
      val mePlayerOpt = data.find {p => p.id == meId}
      val himPlayerOpt = data.find {p => p.id == himId}
      if (mePlayerOpt.isDefined) {removePlayerById (mePlayerOpt.get.id)}
      sender ! PlayerChallengeResp (mePlayerOpt, himPlayerOpt)
    }

    private def waitingPlayers (): Unit = {
      sender ! WaitingPlayersResp (data.toList)
    }

    private def playerEntered (name: String, representative: ActorRef): Unit = {
      val id = nextId
      data.append (DisplayPlayer (id, name, clock (), representative))
      nextId += 1
      sender ! PlayerEnteredResp (id)
    }

    private def playerLeft (id: Int): Unit = {
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
}
