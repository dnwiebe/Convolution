package services

import org.scalatest.path

/**
  * Created by dnwiebe on 6/5/16.
  */
class VestibuleServiceTest extends path.FunSpec {
  describe ("A VestibuleService") {
    val subject = new VestibuleService ()

    describe ("asked about waiting players") {
      val result = subject.waitingPlayers()

      it ("finds none") {
        assert (result === Nil)
      }
    }

    describe ("informed of an entering player") {
      subject.playerEntered (DisplayPlayer ("John"))

      describe ("and asked about waiting players") {
        val result = subject.waitingPlayers ()

        it ("finds one") {
          assert (result.contains (DisplayPlayer ("John")))
        }
      }

      describe ("and another entering player") {
        subject.playerEntered (DisplayPlayer ("Fernando"))

        describe ("and asked about waiting players") {
          val result = subject.waitingPlayers ()

          it ("finds two") {
            assert (result.contains (DisplayPlayer ("John")))
            assert (result.contains (DisplayPlayer ("Fernando")))
          }
        }
      }
    }
  }
}
