package controllers

import javax.inject._

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext.Implicits.global
import akka.stream.Materializer

import services.{GameService, Representative, VestibuleService}
import play.api.mvc._
import play.api.libs.streams._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class VestibuleController @Inject()
    (vestibuleService: VestibuleService, gameService: GameService)
    (implicit system: ActorSystem, materializer: Materializer)
    extends Controller {

  def index () = Action {implicit request =>
    val httpUrl = routes.VestibuleController.socket().absoluteURL()
    val wsUrl = httpToWs (httpUrl)
    Ok (views.html.vestibule (wsUrl))
  }

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => Representative.props(out, vestibuleService))
  }

  def start (meId: Int, himId: Int) = Action.async {
    vestibuleService.playerChallenge (meId, himId).map { optPair =>
println (s"Challenge: $optPair")
      val (mePlayer, himPlayer) = optPair match {
        case (Some (m), Some (h)) => (m, h)
        case _ => throw new UnsupportedOperationException ("Test-drive me")
      }
      gameService.gamePrepare (mePlayer, himPlayer)
      Ok (views.html.gamescreen (mePlayer, himPlayer))
    }
  }

  private def httpToWs (httpUrl: String): String = {
    if (!httpUrl.startsWith ("http")) {throw new IllegalStateException (s"Can't convert $httpUrl from HTTP to WS")}
    "ws" + httpUrl.substring (4)
  }
}
