package controllers

import javax.inject._

import models.ConvolutionBoard
import play.api.mvc._
import presentation.BoardPresentation
import services.GameService

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class GameScreenController @Inject() extends Controller {
  private val service = new GameService ()

  def index (gameId: String) = Action {
    val (id, board) = service.newGame (8)
    val pres = new BoardPresentation (id, board)
    Ok(views.html.gamescreen(pres))
  }

  def move (gameId: String, column: Int, row: Int) = Action {
    service.findGame (gameId) match {
      case None => NotFound
      case Some (previous) => {
        val current = previous.move (column, row)
        service.updateGame (gameId, current)
        val pres = new BoardPresentation (gameId, current)
        Ok (views.html.gamescreen (pres))
      }
    }
  }
}
