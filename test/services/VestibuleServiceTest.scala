package services

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.path

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
  * Created by dnwiebe on 6/5/16.
  */
class VestibuleServiceTest extends path.FunSpec {
  implicit val ec = ExecutionContext.global

  describe ("A VestibuleService with a mocked clock") {
    implicit val system = ActorSystem ()
    val subject = new VestibuleService ()
    var times = List (1234L, 2345L)
    subject.clock = {() =>
      val time = times.head
      times = times.tail
      time
    }

    describe ("asked about waiting players") {
      val result = Await.result (subject.waitingPlayers(), 1 seconds)

      it ("finds none") {
        assert (result === Nil)
      }
    }

    describe ("informed of an entering player") {
      val johnRep = TestProbe ()
      val john = DisplayPlayer (1, "John", 1234L, johnRep.ref)

      subject.playerEntered (john.name, johnRep.ref)

      it ("responds correctly to John") {
        val messages = johnRep.receiveN (2, 1 seconds)
        assert (messages.head === PlayerAccepted (john))
        assert (messages.tail.head === WaitList (List (john)))
      }

      describe ("and another entering player") {
        val fernandoRep = TestProbe ()
        val fernando = DisplayPlayer (2, "Fernando", 2345L, fernandoRep.ref)

        subject.playerEntered (fernando.name, fernandoRep.ref)

        it ("responds correctly to Fernando and John") {
          val fernandoMessages = fernandoRep.receiveN (2, 1 seconds)
          val johnMessages = johnRep.receiveN (3, 1 seconds) // skip over first two messages, grab third

          assert (fernandoMessages.head === PlayerAccepted (fernando))
          val fernandoWaitListMessage = fernandoMessages.tail.head.asInstanceOf[WaitList]
          val fernandoActualPlayers = fernandoWaitListMessage.players.toSet
          assert (fernandoActualPlayers === Set (john, fernando))

          val johnWaitListMessage = johnMessages.tail.tail.head.asInstanceOf[WaitList]
          val johnActualPlayers = johnWaitListMessage.players.toSet
          assert (johnActualPlayers === Set (john, fernando))
        }

        describe ("and instructed to delete one of them") {
          subject.playerLeft (2)

          describe ("and asked about the result") {
            val result = Await.result (subject.waitingPlayers (), 1 seconds)

            it ("finds only one") {
              assert (result === List (john))
            }
          }
        }

        describe ("and informed of a challenge by an existing player") {
          val result = Await.result (subject.playerChallenge (1, 2), 1 seconds)

          it ("returns both players") {
            assert (result === (Some (john), Some (fernando)))
          }

          describe ("and asked whether it has removed the challenging player") {
            val result = Await.result (subject.waitingPlayers (), 1 seconds)

            it ("says yes") {
              assert (result === List(fernando))
            }
          }

          describe ("and informed of a challenge response") {
            val result = Await.result (subject.playerChallenge (2, 1), 1 seconds)

            it ("returns only Fernando, since John is already gone") {
              assert (result === (Some (fernando), None))
            }

            describe ("and asked about remaining players") {
              val result = Await.result (subject.waitingPlayers (), 1 seconds)

              it ("can't find any") {
                assert (result.isEmpty)
              }
            }
          }
        }

        describe ("and informed of a challenge among nonexistent players") {
          val result = Await.result (subject.playerChallenge (41, 42), 1 seconds)

          it ("can't find them") {
            assert (result === (None, None))
          }
        }
      }
    }

    system.terminate()
  }
}
