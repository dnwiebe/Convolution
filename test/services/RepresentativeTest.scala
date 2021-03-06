package services

import org.scalatest.path
import play.api.libs.json._

import scala.concurrent.duration._
import akka.actor._
import akka.testkit.{TestActorRef, TestProbe}
import org.mockito.Mockito._
import services.VestibuleService.PlayerChallengeReq

import scala.util.{Failure, Success}

/**
  * Created by dnwiebe on 6/10/16.
  */

class WaitListTest extends path.FunSpec {
  implicit val system = ActorSystem ()

  describe ("A WaitList message") {
    val billRep = TestProbe ()
    val tedRep = TestProbe ()
    val subject = WaitList (List (
      DisplayPlayer (123, "Bill", 2345L, billRep.ref),
      DisplayPlayer (234, "Ted", 3456L, tedRep.ref)
    ))

    describe ("converted to JSON") {
      val json = subject.toJson

      it ("looks as expected") {
        val jsValue = Json.parse (json)
        assert ((jsValue \ "type").as[String] === "waitList")
        val players = (jsValue \ "data").as[JsArray].value
        val firstPlayer = players.head
        assert ((firstPlayer \ "id").as[Int] === 123)
        assert ((firstPlayer \ "name").as[String] === "Bill")
        assert ((firstPlayer \ "timestamp").as[Long] === 2345L)
        val secondPlayer = players.tail.head
        assert ((secondPlayer \ "id").as[Int] === 234)
        assert ((secondPlayer \ "name").as[String] === "Ted")
        assert ((secondPlayer \ "timestamp").as[Long] === 3456L)
        assert (players.tail.tail === Nil)
      }
    }
  }
}

class PlayerAcceptedTest extends path.FunSpec {
  implicit val system = ActorSystem ()
  describe ("A PlayerAccepted message") {
    val billRep = TestProbe ()
    val subject = PlayerAccepted (DisplayPlayer (123, "Bill", 2345L, billRep.ref))

    describe ("converted to JSON") {
      val json = subject.toJson

      it ("looks as expected") {
        val jsValue = Json.parse (json)
        assert ((jsValue \ "type").as[String] === "acceptPlayer")
        val player = (jsValue \ "data").as[JsObject]
        assert ((player \ "id").as[Int] === 123)
        assert ((player \ "name").as[String] === "Bill")
        assert ((player \ "timestamp").as[Long] === 2345L)
      }
    }
  }
}

class ChallengeTest extends path.FunSpec {
  implicit val system = ActorSystem ()
  describe ("A Challenge message") {
    val billRep = TestProbe ()
    val subject = Challenge (DisplayPlayer (123, "Bill", 2345L, billRep.ref))

    describe ("converted to JSON") {
      val json = subject.toJson

      it ("looks as expected") {
        val jsValue = Json.parse (json)
        assert ((jsValue \ "type").as[String] === "challenge")
        val player = (jsValue \ "data").as[JsObject]
        assert ((player \ "id").as[Int] === 123)
        assert ((player \ "name").as[String] === "Bill")
        assert ((player \ "timestamp").as[Long] === 2345L)
      }
    }
  }
}

class EnterVestibuleTest extends path.FunSpec {
  describe ("Good JSON for an EnterVestibule message") {
    val jsValue = Json.obj (
      "type" -> "enterVestibule",
      "data" -> Json.obj (
        "name" -> "Chubs"
      )
    )

    describe ("converted to a message") {
      val result = Incoming (jsValue)

      it ("comes out as expected") {
        assert (result === Success (EnterVestibule ("Chubs")))
      }
    }
  }

  describe ("Good JSON for a nonexistent message") {
    val jsValue = Json.obj (
      "type" -> "nonexistent",
      "data" -> Json.obj (
        "name" -> "Chubs"
      )
    )

    describe ("converted to a message") {
      val result = Incoming (jsValue).asInstanceOf[Failure[Incoming]]

      it ("returns the correct failure") {
        val e = result.exception.asInstanceOf [NoSuchElementException]
        assert (e.getMessage === "key not found: nonexistent")
      }
    }
  }
}

class RejectGameTest extends path.FunSpec {
  describe ("Good JSON for a RejectGame message") {
    val jsValue = Json.obj (
      "type" -> "rejectGame",
      "data" -> Json.obj (
        "playerId" -> JsNumber (123)
      )
    )

    describe ("converted to a message") {
      val result = Incoming (jsValue)

      it ("comes out as expected") {
        assert (result === Success (RejectGame (123)))
      }
    }
  }
}

class RepresentativeTest extends path.FunSpec {
  implicit val system = ActorSystem ()
  describe ("A VestibuleActor with a mocked out-actor and a mocked VestibuleService") {
    val out = TestProbe ()
    val vestibuleService = mock (classOf[VestibuleService])
    val gameService = mock (classOf[GameService])
    val subject = TestActorRef (new Representative (out.ref, vestibuleService, gameService))

    describe ("sent an EnterVestibule message from Petey") {

      subject ! makeEnterVestibuleString ("Petey")

      it ("calls the service") {
        verify (vestibuleService).playerEntered ("Petey", subject)
      }
    }

    describe ("sent a RejectGame message about player 234") {

      subject ! makeRejectGameString (234)

      it ("informs the game service") {
        verify (gameService).gameReject (234)
      }

      it ("restores the player to the vestibule service") {
//        val response = (actor ? PlayerChallengeReq (meId, himId)).mapTo[PlayerChallengeResp]
//        response.map {r => (r.mePlayerOpt, r.himPlayerOpt)}
        verify (vestibuleService).playerReentered ()
      }
    }

    describe ("sent an Outgoing message from inside") {
      case class TestOutgoing () extends Outgoing ("booga") {
        def toJsValue: JsValue = JsString ("whop")
      }

      subject ! TestOutgoing ()

      it ("translates the message") {
        val messages = out.receiveN (1)
        val json = messages.head.asInstanceOf[String]
        assert (json === """{"type":"booga","data":"whop"}""", 1 seconds)
      }
    }
  }

  private def makeEnterVestibuleString (name: String): String = {
    val jsValue = Json.obj (
      "type" -> "enterVestibule",
      "data" -> Json.obj (
        "name" -> name
      )
    )
    jsValue.toString ()
  }

  private def makeRejectGameString (playerId: Int): String = {
    val jsValue = Json.obj (
      "type" -> "rejectGame",
      "data" -> Json.obj (
        "playerId" -> playerId
      )
    )
    jsValue.toString ()
  }
}
