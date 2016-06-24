package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestProbe
import org.scalatest.path
import org.mockito.Mockito._
import play.api.test.FakeRequest
import services.{DisplayPlayer, GameService, VestibuleService}
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by dnwiebe on 6/5/16.
  */
class VestibuleControllerTest extends path.FunSpec {
  implicit val ec = ExecutionContext.global

  describe ("A VestibuleController with mocked services") {
    val vestibuleService = mock (classOf[VestibuleService])
    val gameService = mock (classOf[GameService])
    implicit val system = ActorSystem ()
    val materializer = ActorMaterializer ()
    val subject = new VestibuleController (vestibuleService, gameService) (system, materializer)

    describe ("accessed by a new player") {
      val result = subject.index ().apply (FakeRequest ())
      val html = contentAsString (result)

      it ("shows the proper page") {
        assert (html.contains ("Enter your name"))
        assert (html.contains ("<script>Vestibule ('ws:///vestibule/socket');</script>"))
      }
    }

    describe ("exited to game start with good ids") {
      val bill = DisplayPlayer (123, "Bill", 4321L, TestProbe ().ref)
      val ted = DisplayPlayer (234, "Ted", 4321L, TestProbe ().ref)
      when (vestibuleService.playerChallenge (123, 234)).thenReturn (Future {(Some (bill), Some (ted))})

      val result = subject.start (123, 234).apply (FakeRequest ())
      val html = contentAsString (result)

      it ("shows the proper page") {
        assert (html.contains ("Waiting for opponent Ted"))
        assert (html.contains ("<script>GameScreen (123, 234, 'ws:///games/socket/123/234');</script>"))
      }

      it ("informs game service of entry") {
        verify (gameService).gamePrepare (bill, ted)
      }
    }
  }
}
