package services

import org.scalatest.path

/**
  * Created by dnwiebe on 6/4/16.
  */
class GameServiceTest extends path.FunSpec {
  describe ("A GameService") {
    val subject = new GameService ()

    describe ("when directed to create two games") {
      val (oneId, oneGame) = subject.newGame (8)
      val (anotherId, anotherGame) = subject.newGame (5)

      it ("returns two different IDs") {
        assert (oneId != anotherId)
      }

      it ("returns games with appropriate orders") {
        assert (oneGame.order === 8)
        assert (anotherGame.order === 5)
      }

      it ("returns a properly randomized game") {
        val isSetButNot: (Int, Int, Int) => Boolean = {(col, row, value) =>
          val cell = anotherGame.valueAt (col, row)
          cell.isDefined && !cell.contains (value)
        }
        val upLeft = isSetButNot (0, 0, 1)
        val upRight = isSetButNot (4, 0, 5)
        val downLeft = isSetButNot (0, 4, 21)
        val downRight = isSetButNot (4, 4, 25)

        assert (upLeft || upRight || downLeft || downRight)
      }

      describe ("and asked to find one of them") {
        val oneFoundGame = subject.findGame (oneId)

        it ("returns the one we're looking for") {
          assert (oneFoundGame === Some (oneGame))
        }

        describe ("and it is removed") {
          subject.removeGame (oneId)

          describe ("and we look for it again") {
            val result = subject.findGame (oneId)

            it ("it's gone") {
              assert (result === None)
            }
          }
        }
      }

      describe ("and one of them is modified") {
        val modifiedAnotherGame = anotherGame.move (0, 0)

        describe ("and updated") {
          subject.updateGame (anotherId, modifiedAnotherGame)

          describe ("and found") {
            val result = subject.findGame (anotherId)

            it ("the found game is the same as the modified one") {
              assert (result === Some (modifiedAnotherGame))
            }

            it ("the found game is not the same as the unmodified one") {
              assert (result != Some (anotherGame))
            }
          }
        }
      }
    }

    describe ("when directed to retrieve a nonexistent game") {
      val result = subject.findGame ("booga booga")

      it ("returns None") {
        assert (result === None)
      }
    }
  }
}
