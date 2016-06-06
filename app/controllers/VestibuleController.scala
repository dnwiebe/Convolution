package controllers

import javax.inject._

import play.api.mvc._
import services.{DisplayPlayer, GameService, VestibuleService}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class VestibuleController @Inject() (vestibuleService: VestibuleService, gameService: GameService) extends Controller {

  def index () = Action {
    Ok (views.html.vestibule (Nil, None))
  }

  def enter () = Action {implicit request =>
    val form = request.body.asFormUrlEncoded
    val name = form.get.get("name-field").get.head
    val player = DisplayPlayer (name)
    vestibuleService.playerEntered (player)
    val waitingPlayers = vestibuleService.waitingPlayers()
    Ok (views.html.vestibule (waitingPlayers, Some (player)))
  }

  def start (challenger: String, victim: String) = Action {
    Ok
  }
}
