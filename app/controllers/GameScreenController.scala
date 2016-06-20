package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer
import services.{GameService, VestibuleService}
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

/// Spike
@Singleton
class GameScreenController @Inject
    (vestibuleService: VestibuleService, gameService: GameService)
    (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  def start (challenger: Int, victim: Int) = Action {
    Ok
  }
}
/// Spike
