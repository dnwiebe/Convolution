package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.duration._
import services.{GameService, Representative, VestibuleService}
import play.api.mvc._
import play.api.libs.streams._

import scala.concurrent.Await

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class VestibuleController @Inject()
    (vestibuleService: VestibuleService, gameService: GameService)
    (implicit system: ActorSystem, materializer: Materializer)
    extends Controller {

  def index () = Action {
    Ok (views.html.vestibule ())
  }

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => Representative.props(out, vestibuleService))
  }

  def start (meId: Int, himId: Int) = Action {
    val optPair = Await.result (vestibuleService.playerChallenge (meId, himId), 1 seconds)
    val (mePlayer, himPlayer) = optPair match {
      case (Some(m), Some (h)) => (m, h)
      case _ => throw new UnsupportedOperationException ("Test-drive me")
    }
    gameService.gameEnter (mePlayer, himPlayer)
    Ok (views.html.gamescreen (mePlayer, himPlayer))
  }
}
