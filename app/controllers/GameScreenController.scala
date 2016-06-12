package controllers

import javax.inject._

import play.api.mvc._
import presentation.BoardPresentation
import services.GameService

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

/// Spike
@Singleton
class GameScreenController @Inject() extends Controller {
  private val service = new GameService ()

  def start (challenger: Int, victim: Int) = Action {
    Ok
  }
}
/// Spike
