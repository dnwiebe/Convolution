package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Singleton
import models.ConvolutionBoard
import play.api.libs.json.{JsBoolean, JsObject}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
  * Created by dnwiebe on 6/3/16.
  */

case class RejectGame (player: DisplayPlayer) extends Outgoing ("rejectGame") with PlayerMessage {
  def toJsValue = toJsValue (player)
}

case class StartGame (board: ConvolutionBoard, isHorizontal: Boolean, opponent: DisplayPlayer)
    extends Outgoing ("startGame") with BoardMessage with PlayerMessage {
  def toJsValue = JsObject (Seq (
    "board" -> toJsValue (board), "isHorizontal" -> JsBoolean (isHorizontal), "opponent" -> toJsValue (opponent)
  ))
}

case class Turn (board: ConvolutionBoard, yourTurn: Boolean) extends Outgoing ("turn") with BoardMessage {
  def toJsValue = JsObject (Seq (
    "board" -> toJsValue (board), "yourTurn" -> JsBoolean (yourTurn)
  ))
}

@Singleton
class GameService (implicit val system: ActorSystem) {
  case class Game (horizontal: DisplayPlayer, vertical: DisplayPlayer, var board: ConvolutionBoard)
  var boardFactory: () => ConvolutionBoard = {() => ConvolutionBoard (8).randomize ().build}
  val actorRef = GameServiceActor (this)

  def gamePrepare (mePlayer: DisplayPlayer, himPlayer: DisplayPlayer): Unit = {
    actorRef ! GamePrepareReq (mePlayer, himPlayer)
  }

  def gameReject (himPlayerId: Int): Unit = {
    actorRef ! GameRejectReq (himPlayerId)
  }

  def gameAccept (mePlayerId: Int): Unit = {
    actorRef ! GameAcceptReq (mePlayerId)
  }

  def acceptMove (playerId: Int, coordinate: Int): Unit = {
    actorRef ! AcceptMoveReq (playerId, coordinate)
  }

  def findGame (playerId: Int): Option[Game] = {
    implicit val timeout = Timeout (1 seconds)
    val future = actorRef ? FindGameReq (playerId)
    Await.result (future, 1 seconds).asInstanceOf[Option[Game]]
  }

  private case class GamePrepareReq (mePlayer: DisplayPlayer, himPlayer: DisplayPlayer)
  private case class GameRejectReq (himPlayerId: Int)
  private case class GameAcceptReq (mePlayerId: Int)
  private case class AcceptMoveReq (playerId: Int, coordinate: Int)
  private case class FindGameReq (playerId: Int)
  private case class FindGameResp (gameOpt: Option[Game])

  object GameServiceActor {
    def apply (service: GameService)(implicit system: ActorSystem): ActorRef = {
      system.actorOf (Props (classOf[GameServiceActor], service))
    }
  }

  class GameServiceActor () extends Actor {
    private val incipientGames = scala.collection.mutable.Map[Integer, Game] ()
    private val gamesInProgress = scala.collection.mutable.Map[Integer, Game] ()

    def receive = {
      case msg: GamePrepareReq => gamePrepare (msg.mePlayer, msg.himPlayer)
      case msg: GameRejectReq => gameReject (msg.himPlayerId)
      case msg: GameAcceptReq => gameAccept (msg.mePlayerId)
      case msg: AcceptMoveReq => acceptMove (msg.playerId, msg.coordinate)
      case msg: FindGameReq => findGame (msg.playerId)
    }

    private def gamePrepare (mePlayer: DisplayPlayer, himPlayer: DisplayPlayer): Unit = {
      val game = Game (mePlayer, himPlayer, boardFactory ())
      incipientGames(himPlayer.id) = game
    }

    private def gameReject (himPlayerId: Int): Unit = {
      incipientGames.get (himPlayerId) match {
        case None => {
          println (s"Player with id ${himPlayerId} failed to reject nonexistent game - ignoring")
        }
        case Some (game) => {
          game.horizontal.representative ! RejectGame (game.vertical)
        }
      }
    }

    private def gameAccept (mePlayerId: Int): Unit = {
      incipientGames.remove (mePlayerId) match {
        case None => {
          println (s"Player with id ${mePlayerId} failed to accept nonexistent game - ignoring")
        }
        case Some (game) => {
          gamesInProgress (game.horizontal.id) = game
          gamesInProgress (game.vertical.id) = game
          game.horizontal.representative ! StartGame (game.board, true, game.vertical)
          game.vertical.representative ! StartGame (game.board, false, game.horizontal)
        }
      }
    }

    private def acceptMove (playerId: Int, coordinate: Int): Unit = {
      gamesInProgress.get (playerId) match {
        case None => println (s"Player with id ${playerId} is not known to be playing any games - ignoring")
        case Some (game) => {
          if (game.board.isGameOver) {
            println (s"Player with id ${playerId} tried to move after the game was over - ignoring")
          }
          else if (game.horizontal.id == playerId) {
            acceptHorizontalMove (game, coordinate)
          }
          else {
            acceptVerticalMove (game, coordinate)
          }
        }
      }
    }

    private def findGame (playerId: Int): Unit = {
      sender ! gamesInProgress.get (playerId)
    }

    private def acceptHorizontalMove (game: Game, coordinate: Int): Unit = {
      if (!game.board.horizontalIsNext.contains (true)) {
        println (s"Horizontal player ${game.horizontal.name} tried to move out of turn - ignoring")
        return
      }
      game.board = game.board.move (coordinate, game.board.fixedCoordinate)
      game.horizontal.representative ! Turn (game.board, false)
      game.vertical.representative ! Turn (game.board, true)
      checkForGameComplete (game)
    }

    private def acceptVerticalMove (game: Game, coordinate: Int): Unit = {
      if (!game.board.horizontalIsNext.contains (false)) {
        println (s"Vertical player ${game.vertical.name} tried to move out of turn - ignoring")
        return
      }
      game.board = game.board.move (game.board.fixedCoordinate, coordinate)
      game.vertical.representative ! Turn (game.board, false)
      game.horizontal.representative ! Turn (game.board, true)
      checkForGameComplete (game)
    }

    private def checkForGameComplete (game: Game): Unit = {
      if (!game.board.isGameOver) {return}
      gamesInProgress.remove (game.horizontal.id)
      gamesInProgress.remove (game.vertical.id)
    }
  }
}
