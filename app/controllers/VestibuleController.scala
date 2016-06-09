package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.mvc._
import services.{DisplayPlayer, GameService, VestibuleActor, VestibuleService}
import play.api.mvc._
import play.api.libs.streams._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class VestibuleController @Inject() (vestibuleService: VestibuleService, gameService: GameService) (implicit system: ActorSystem) extends Controller {

  def index () = Action {
    Ok (views.html.vestibule (Nil, None))
  }

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => VestibuleActor.props(out))
  }

  def enter () = Action {implicit request =>
    val form = request.body.asFormUrlEncoded
    val name = form.get.get("name-field").get.head
    val player = DisplayPlayer (name)
    vestibuleService.playerEntered (player)
    val waitingPlayers = vestibuleService.waitingPlayers()
    Ok (views.html.vestibule (waitingPlayers, Some (player)))
  }
}
