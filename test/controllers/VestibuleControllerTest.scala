package controllers

import org.scalatest.path
import org.mockito.Mockito._
import play.api.test.FakeRequest
import services.{DisplayPlayer, GameService, VestibuleService}
import play.api.test._
import play.api.test.Helpers._
import play.mvc.Http.RequestBody
/**
  * Created by dnwiebe on 6/5/16.
  */
class VestibuleControllerTest extends path.FunSpec {
  describe ("A VestibuleController with mocked services") {
    val vestibuleService = mock (classOf[VestibuleService])
    val gameService = mock (classOf[GameService])
    val subject = new VestibuleController (vestibuleService, gameService)

    describe ("given an empty vestibule") {
      when (vestibuleService.waitingPlayers ()).thenReturn (Nil)

      describe ("visited by a new player") {
        val result = subject.index ().apply(FakeRequest())
        val html = contentAsString (result)

        it ("shows no waiting players") {
          assert (!html.contains ("""class="waiting-player""""))
        }

        it ("demands the new player's name") {
          assert (html.contains ("Enter your name"))
        }

        it ("does not allow the new player to choose an opponent") {
          assert (!html.contains ("choose an opponent"))
        }

        describe ("when the player enters a name") {
          val request = FakeRequest ("POST", "/vestibule/enter").withFormUrlEncodedBody(("name-field", "Buster"))
          val result = subject.enter ().apply (request)
          val html = contentAsString (result)

          it ("shows no waiting players") {
            assert (!html.contains ("""class="waiting-player""""))
          }

          it ("does not demand the new player's name") {
            assert (!html.contains ("Enter your name"))
          }

          it ("does not allow the new player to choose an opponent") {
            assert (!html.contains ("choose an opponent"))
          }

          it ("displays the player's name") {
            assert (html.contains ("Waiting to be chosen by an opponent: Buster"))
          }

          it ("informs the VestibuleService of the player's arrival") {
            verify (vestibuleService).playerEntered (DisplayPlayer ("Buster"))
          }
        }
      }
    }

    describe ("given a vestibule with players already in it") {
      when (vestibuleService.waitingPlayers ()).thenReturn (List (
        DisplayPlayer ("Bill"),
        DisplayPlayer ("Ted")
      ))

      describe ("visited by a new player") {
        val result = subject.index ().apply(FakeRequest())
        val html = contentAsString (result)

        it ("shows no waiting players") {
          assert (!html.contains ("""class="waiting-player""""))
        }

        it ("demands the new player's name") {
          assert (html.contains ("Enter your name"))
        }

        it ("does not allow the new player to choose an opponent") {
          assert (!html.contains ("choose an opponent"))
        }

        describe ("when the player enters a name") {
          val request = FakeRequest ("POST", "/vestibule/enter").withFormUrlEncodedBody(("name-field", "Buster"))
          val result = subject.enter ().apply (request)
          val html = contentAsString (result)

          it ("shows waiting players") {
            assert (html.contains ("""class="waiting-player""""))
            assert (html.contains ("""Bill"""))
            assert (html.contains ("""Ted"""))
          }

          it ("does not demand the new player's name") {
            assert (!html.contains ("Enter your name"))
          }

          it ("allows the new player to choose an opponent") {
            assert (html.contains ("choose an opponent"))
          }

          it ("displays the player's name") {
            assert (html.contains ("Waiting to be chosen by an opponent: Buster"))
          }

          it ("informs the VestibuleService of the player's arrival") {
            verify (vestibuleService).playerEntered (DisplayPlayer ("Buster"))
          }
        }
      }
    }
  }
}
