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
  implicit val system = ActorSystem ()

  describe ("A VestibuleService with a mocked clock") {
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
      val john = DisplayPlayer (1, "John", 1234L, TestProbe ().ref)

      val firstId = Await.result (subject.playerEntered (john.name, john.representative), 1 seconds)

      it ("returns the correct ID") {
        assert (firstId === 1)
      }

      describe ("and asked about waiting players") {
        val result = Await.result (subject.waitingPlayers (), 1 seconds)

        it ("finds one") {
          assert (result.contains (john))
        }
      }

      describe ("and another entering player") {
        val fernando = DisplayPlayer (2, "Fernando", 2345L, TestProbe ().ref)

        val secondId = Await.result (subject.playerEntered (fernando.name, fernando.representative), 1 seconds)

        it ("returns the correct ID") {
          assert (secondId === 2)
        }

        describe ("and asked about waiting players") {
          val result = Await.result (subject.waitingPlayers (), 1 seconds)

          it ("finds two") {
            assert (result.contains (john))
            assert (result.contains (fernando))
          }
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
  }
}
