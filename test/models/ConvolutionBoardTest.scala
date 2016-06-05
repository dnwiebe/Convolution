package models

import org.scalatest.path

/**
  * Created by dnwiebe on 6/1/16.
  */
class ConvolutionBoardTest extends path.FunSpec {
  describe ("An order-8 Convolution board, derandomized") {
    val first = derandomize (ConvolutionBoard (8)).build

    it ("has the specified order") {
      assert (first.order === 8)
    }

    it ("contains the numbers from the generator in the correct order") {
      for (col <- 0 until 8; row <- 0 until 8) {
        assert (first.valueAt (col, row) === Some ((row * 8) + col + 1), s"failure at ($col, $row)")
      }
    }

    it ("the next player is horizontal") {
      assert (first.horizontalIsNext === Some (true))
    }

    it ("has a fixed coordinate of 0") {
      assert (first.fixedCoordinate === 0)
    }

    it ("has no score") {
      assert (first.horizontalScore === 0)
      assert (first.verticalScore === 0)
    }

    it ("is not game over") {
      assert (first.isGameOver === false)
    }

    it ("has no winner") {
      assert (first.horizontalIsWinner === None)
    }

    describe ("when a legal move is made") {
      val second = first.move (3, 0)

      it ("blanks out the value") {
        assert (second.valueAt (3, 0) === None)
      }

      it ("sets the next player to vertical") {
        assert (second.horizontalIsNext === Some (false))
      }

      it ("has a fixed coordinate of 3") {
        assert (second.fixedCoordinate === 3)
      }

      it ("has the expected score") {
        assert (second.horizontalScore === 4)
        assert (second.verticalScore === 0)
      }

      it ("is not game over") {
        assert (second.isGameOver === false)
      }

      it ("has no winner") {
        assert (second.horizontalIsWinner === None)
      }

      describe ("and another legal move is made") {
        val third = second.move (3, 5)

        it ("blanks out the value") {
          assert (third.valueAt (3, 5) === None)
        }

        it ("sets the next player back to horizontal") {
          assert (third.horizontalIsNext === Some (true))
        }

        it ("has a fixed coordinate of 5") {
          assert (third.fixedCoordinate === 5)
        }

        it ("has the expected score") {
          assert (third.horizontalScore === 4)
          assert (third.verticalScore === 44)
        }

        it ("is not game over") {
          assert (third.isGameOver === false)
        }

        it ("has no winner") {
          assert (third.horizontalIsWinner === None)
        }
      }

      describe ("and a move into an already-empty space is attempted") {
        var exception: Exception = null
        try {
          second.move (3, 0)
        }
        catch {
          case e: IllegalArgumentException => exception = e
        }

        it ("throws the expected exception") {
          assert (exception.getMessage === "Cannot move at (3, 0): already empty")
        }
      }

      describe ("and a move outside the designated column is attempted") {
        var exception: Exception = null
        try {
          second.move (2, 2)
        }
        catch {
          case e: IllegalArgumentException => exception = e
        }

        it ("throws the expected exception") {
          assert (exception.getMessage === "Vertical player must choose from column 3, not (2, 2)")
        }
      }
    }

    describe ("when a move outside the designated row is attempted") {
      var exception: Exception = null
      try {
        first.move (3, 4)
      }
      catch {
        case e: IllegalArgumentException => exception = e
      }

      it ("throws the expected exception") {
        assert (exception.getMessage === "Horizontal player must choose from row 0, not (3, 4)")
      }
    }

    describe ("An order-2 Convolution board, derandomized") {
      val subject = derandomize (ConvolutionBoard (2)).build

      it ("presents the expected game state") {
        assert (subject.horizontalIsNext === Some (true))
        assert (subject.horizontalScore === 0)
        assert (subject.verticalScore === 0)
        assert (subject.isGameOver === false)
        assert (subject.horizontalIsWinner === None)
      }

      describe ("after the first move") {
        val first = subject.move (0, 0)

        it ("presents the expected game state") {
          assert (first.horizontalIsNext === Some (false))
          assert (first.horizontalScore === 1)
          assert (first.verticalScore === 0)
          assert (first.isGameOver === false)
          assert (first.horizontalIsWinner === None)
        }

        describe ("after the second move") {
          val second = first.move (0, 1)

          it ("presents the expected game state") {
            assert (second.horizontalIsNext === Some (true))
            assert (second.horizontalScore === 1)
            assert (second.verticalScore === 3)
            assert (second.isGameOver === false)
            assert (second.horizontalIsWinner === None)
          }

          describe ("after the third move") {
            val third = second.move (1, 1)

            it ("presents the expected game state") {
              assert (third.horizontalIsNext === Some (false))
              assert (third.horizontalScore === 5)
              assert (third.verticalScore === 3)
              assert (third.isGameOver === false)
              assert (third.horizontalIsWinner === None)
            }

            describe ("after the fourth move") {
              val fourth = third.move (1, 0)

              it ("presents the expected game state") {
                assert (fourth.horizontalIsNext === None)
                assert (fourth.horizontalScore === 5)
                assert (fourth.verticalScore === 5)
                assert (fourth.isGameOver === true)
                assert (fourth.horizontalIsWinner === None)
              }
            }
          }
        }
      }
    }

    describe ("An order-5 board with a real RNG") {
      val subject = ConvolutionBoard (5).randomize ().build

      it ("displays every number from 1 to 25 exactly once") {
        val coords = for (column <- 0 until 5; row <- 0 until 5) yield (column, row)
        val allNumbers = coords.foldLeft (Set[Int] ()) { (soFar, elem) =>
          val (column, row) = elem
          soFar + subject.valueAt (column, row).get
        }
        assert (allNumbers.size === 25)
        val incorrectNumbers = (1 to 25).foldLeft (allNumbers) {(soFar, elem) =>
          soFar - elem
        }
        assert (incorrectNumbers.toList.sortBy {x => x}.isEmpty)
      }
    }
  }

  private def derandomize (builder: ConvolutionBoard.Builder): ConvolutionBoard.Builder = {
    for (row <- 0 until builder.order; col <- 0 until builder.order) {
      builder.valueAt (col, row, Some ((row * builder.order) + col + 1))
    }
    builder
  }
}
