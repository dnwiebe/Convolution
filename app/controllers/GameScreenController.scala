package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import services.{GameService, Representative, VestibuleService}
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

@Singleton
class GameScreenController @Inject
    (vestibuleService: VestibuleService, gameService: GameService)
    (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  def socket (mePlayerId: Int, himPlayerId: Int) = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => Representative.props(out, vestibuleService, gameService))
  }
}
