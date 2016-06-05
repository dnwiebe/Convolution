package presentation

import models.ConvolutionBoard

/**
  * Created by dnwiebe on 6/2/16.
  */
class BoardPresentation (gameId: String, board: ConvolutionBoard) {
  def order = board.order
  def horizontalScore = board.horizontalScore
  def verticalScore = board.verticalScore

  def cellString (column: Int, row: Int) = {
    board.valueAt (column, row) match {
      case None => s"""<img id="cell-$column-$row" class="empty ${cellClass (column, row)}" src="/assets/images/emptycell.png" height="30px" width="30px" alt="Empty cell">"""
      case Some (n) => {
        val inner = s"""<div id="cell-$column-$row" class="${cellClass (column, row)}">$n</div>"""
        (board.horizontalIsNext, board.fixedCoordinate) match {
          case (Some (true), f) if row == f => makeClickable (inner, column, row)
          case (Some (false), f) if column == f => makeClickable (inner, column, row)
          case _ => inner
        }
      }
    }
  }

  def cellClass (column: Int, row: Int): String = {
    (board.horizontalIsNext, board.fixedCoordinate) match {
      case (Some (true), f) if row == f => "hfixed"
      case (Some (false), f) if column == f => "vfixed"
      case _ => "unselected"
    }
  }

  private def makeClickable (inner: String, column: Int, row: Int): String = {
    s"""<a href="/games/${gameId}/move/$column/$row">$inner</a>"""
  }
}
