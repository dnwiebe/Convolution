package services

import org.scalatest.path
import play.api.libs.json.{JsArray, JsObject, Json}
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor._
import akka.testkit.TestProbe
import org.mockito.Mockito._

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
        assert ((jsValue \ "opcode").as[String] === "waitList")
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
        assert ((jsValue \ "opcode").as[String] === "acceptPlayer")
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
        assert ((jsValue \ "opcode").as[String] === "challenge")
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
      "opcode" -> "enterVestibule",
      "data" -> Json.obj (
        "name" -> "Chubs"
      )
    )

    describe ("converted to a message") {
      val result = Incoming (classOf[EnterVestibule], jsValue)

      it ("comes out as expected") {
        assert (result === Some (EnterVestibule ("Chubs")))
      }
    }
  }

  describe ("Good JSON for a nonexistent message") {
    val jsValue = Json.obj (
      "opcode" -> "nonexistent",
      "data" -> Json.obj (
        "name" -> "Chubs"
      )
    )

    describe ("converted to a message") {
      val result = Incoming (jsValue)

      it ("becomes None") {
        assert (result === None)
      }
    }
  }
}

class RepresentativeTest extends path.FunSpec {
  implicit val system = ActorSystem ()
  describe ("A VestibuleActor with a mocked out-actor and a mocked VestibuleService") {
    val out = TestProbe ()
    val vestibuleService = mock (classOf[VestibuleService])
    val subject = Representative (out.ref, vestibuleService) (system)

    describe ("sent an EnterVestibule message from Petey") {
//      when (vestibuleService.playerEntered ("Petey", subject)).thenReturn (345)
//      val petey = DisplayPlayer (345, "Petey", 4567L)
//      when (vestibuleService.waitingPlayerById (345)).thenReturn (Some (petey))
//      val bill = DisplayPlayer (123, "Bill", 2345L)
//      val ted = DisplayPlayer (234, "Ted", 3456L)
//      when (vestibuleService.waitingPlayers ()).thenReturn (List (bill, ted))

      subject ! makeEnterVestibuleString ("Petey")

      it ("calls the service") {
        verify (vestibuleService).playerEntered ("Petey", subject)

//        val outgoingMessages = out.receiveN (2, 100 millis)
//
//        val playerAccepted = Json.parse (outgoingMessages.head.asInstanceOf[String])
//        assert ((playerAccepted \ "opcode").as[String] === "acceptPlayer")
//        assert ((playerAccepted \ "data" \ "id").as[Int] === 345)
//        assert ((playerAccepted \ "data" \ "name").as[String] === "Petey")
//        assert ((playerAccepted \ "data" \ "timestamp").as[Long] === 4567L)
//
//        val waitList = Json.parse (outgoingMessages.tail.head.asInstanceOf[String])
//        assert ((waitList \ "opcode").as[String] === "waitList")
//        val players = (waitList \ "data").as[JsArray].value.map {jsValue => DisplayPlayer (
//          (jsValue \ "id").as[Int],
//          (jsValue \ "name").as[String],
//          (jsValue \ "timestamp").as[Long]
//        )}
//        assert (players.contains (bill))
//        assert (players.contains (ted))
//        assert (players.size === 2)
      }
    }
  }

  private def makeEnterVestibuleString (name: String): String = {
    val jsValue = Json.obj (
      "opcode" -> "enterVestibule",
      "data" -> Json.obj (
        "name" -> name
      )
    )
    jsValue.toString ()
  }
}
