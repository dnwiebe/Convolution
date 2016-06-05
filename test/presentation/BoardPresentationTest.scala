package presentation

import models.ConvolutionBoard
import org.mockito.Mockito._
import org.scalatest.path

/**
  * Created by dnwiebe on 6/2/16.
  */
class BoardPresentationTest extends path.FunSpec {
  describe ("A BoardPresentation with a mock ConvolutionBoard in it") {
    val board = mock (classOf[ConvolutionBoard])
    val subject = new BoardPresentation ("1234", board)

    describe ("asked for its order") {
      when (board.order).thenReturn (8)
      val result = subject.order

      it ("delegates to its board") {
        assert (result === 8)
      }
    }

    describe ("asked for the horizontal score") {
      when (board.horizontalScore).thenReturn (47)
      val result = subject.horizontalScore

      it ("delegates to its board") {
        assert (result === 47)
      }
    }

    describe ("asked for the vertical score") {
      when (board.verticalScore).thenReturn (74)
      val result = subject.verticalScore

      it ("delegates to its board") {
        assert (result === 74)
      }
    }

    describe ("asked for the contents of a table cell") {
      describe ("not on any fixed coordinate") {
        when (board.fixedCoordinate).thenReturn (5)
        when (board.horizontalIsNext).thenReturn (Some (true))

        describe ("that is empty") {
          when (board.valueAt (2, 3)).thenReturn (None)

          val result = subject.cellString (2, 3)

          it ("returns an image link") {
            assert (result === """<img id="cell-2-3" class="empty unselected" src="/assets/images/emptycell.png" height="30px" width="30px" alt="Empty cell">""")
          }
        }

        describe ("that is full") {
          when (board.valueAt (2, 3)).thenReturn (Some (54))

          val result = subject.cellString (2, 3)

          it ("returns identified number text in a span") {
            assert (result === """<div id="cell-2-3" class="unselected">54</div>""")
          }
        }
      }

      describe ("on the horizontal fixed coordinate") {
        when (board.fixedCoordinate).thenReturn (3)
        when (board.horizontalIsNext).thenReturn (Some (true))

        describe ("that is empty") {
          when (board.valueAt (2, 3)).thenReturn (None)

          val result = subject.cellString (2, 3)

          it ("returns an image link") {
            assert (result === """<img id="cell-2-3" class="empty hfixed" src="/assets/images/emptycell.png" height="30px" width="30px" alt="Empty cell">""")
          }
        }

        describe ("that is full") {
          when (board.valueAt (2, 3)).thenReturn (Some (54))

          val result = subject.cellString (2, 3)

          it ("returns clickable identified number text in a div") {
            assert (result === """<a href="/games/1234/move/2/3"><div id="cell-2-3" class="hfixed">54</div></a>""")
          }
        }
      }

      describe ("on the vertical fixed coordinate") {
        when (board.fixedCoordinate).thenReturn (2)
        when (board.horizontalIsNext).thenReturn (Some (false))

        describe ("that is empty") {
          when (board.valueAt (2, 3)).thenReturn (None)

          val result = subject.cellString (2, 3)

          it ("returns an image link") {
            assert (result === """<img id="cell-2-3" class="empty vfixed" src="/assets/images/emptycell.png" height="30px" width="30px" alt="Empty cell">""")
          }
        }

        describe ("that is full") {
          when (board.valueAt (2, 3)).thenReturn (Some (45))

          val result = subject.cellString (2, 3)

          it ("returns clickable identified number text in a div") {
            assert (result === """<a href="/games/1234/move/2/3"><div id="cell-2-3" class="vfixed">45</div></a>""")
          }
        }
      }
    }
  }
}
