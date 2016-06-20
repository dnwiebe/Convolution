package services

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import models.ConvolutionBoard
import org.scalatest.path
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.libs.json.{JsBoolean, JsObject, Json}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by dnwiebe on 6/4/16.
  */
class GameServiceTest extends path.FunSpec with PlayerMessage with BoardMessage {
  describe ("A GameService") {
    implicit val system = ActorSystem ()
    val subject = new GameService ()

    describe ("when directed to prepare for a game") {
      val meRep = TestProbe ()
      val me = DisplayPlayer (123, "Billy", 1234L, meRep.ref)
      val himRep = TestProbe ()
      val him = DisplayPlayer (234, "Teddy", 2345L, himRep.ref)

      subject.gamePrepare (me, him)

      it ("the game can be found from both player IDs") {
        val meGameOpt = subject.findGame (123)
        val himGameOpt = subject.findGame (234)
        assert (meGameOpt eq himGameOpt)
      }

      describe ("and the game is accepted by the him player") {
        subject.gameAccept (him.id)

        describe ("hands out a message to each player") {
          val meMessage = meRep.receiveOne (1 seconds).asInstanceOf [StartGame]
          val himMessage = himRep.receiveOne (1 seconds).asInstanceOf [StartGame]

          it ("where the me player is horizontal") {
            assert (meMessage.isHorizontal === true)
          }

          it ("where the him player is vertical") {
            assert (himMessage.isHorizontal === false)
          }

          it ("where the same game board is given to each player") {
            assert (meMessage.board eq himMessage.board)
          }

          describe ("and a horizontal move comes in from the me player") {
            subject.acceptMove (123, 7)

            describe ("hands out Turn messages") {
              val meMessage = meRep.receiveOne (1 seconds).asInstanceOf [Turn]
              val himMessage = himRep.receiveOne (1 seconds).asInstanceOf [Turn]

              it ("where the same (properly modified) board is sent to both players") {
                val board = meMessage.board
                assert (board.valueAt (7, 0) === None)
                assert (himMessage.board eq board)
              }

              it ("where it is now the him player's turn") {
                assert (meMessage.yourTurn === false)
                assert (himMessage.yourTurn === true)
              }

              describe ("and a vertical move comes in from the him player") {
                subject.acceptMove (234, 7)

                describe ("hands out Turn messages") {
                  val meMessage = meRep.receiveOne (1 seconds).asInstanceOf [Turn]
                  val himMessage = himRep.receiveOne (1 seconds).asInstanceOf [Turn]

                  it ("where the same (properly modified) board is sent to both players") {
                    val board = meMessage.board
                    assert (board.valueAt (7, 7) === None)
                    assert (himMessage.board eq board)
                  }

                  it ("where it is now the me player's turn") {
                    assert (meMessage.yourTurn === true)
                    assert (himMessage.yourTurn === false)
                  }
                }
              }

              describe ("and a move from the horizontal player comes in at the wrong time") {
                subject.acceptMove (123, 7)

                it ("returns no messages") {
                  meRep.expectNoMsg (100 millis)
                  himRep.expectNoMsg (100 millis)
                }
              }
            }
          }

          describe ("and a move from the vertical player comes in at the wrong time") {
            subject.acceptMove (234, 7)

            it ("returns no messages") {
              meRep.expectNoMsg (100 millis)
              himRep.expectNoMsg (100 millis)
            }
          }

          describe ("and a move comes in from a player not known to be playing any games") {
            subject.acceptMove (-1, 7)

            it ("returns no messages") {
              meRep.expectNoMsg (100 millis)
              himRep.expectNoMsg (100 millis)
            }
          }
        }
      }

      describe ("and the game is rejected by the him player") {
        subject.gameReject (him.id)

        describe ("sends a rejected message to the me player") {
          val meMessage = meRep.receiveOne (1 seconds).asInstanceOf[RejectGame]

          it ("where the player is the him player") {
            assert (meMessage.player === him)
          }

          describe ("and then accepted by the me player") {
            subject.gameAccept (me.id)

            it ("returns no messages") {
              meRep.expectNoMsg (100 millis)
              himRep.expectNoMsg (100 millis)
            }
          }
        }
      }

      describe ("and the game is rejected by the me player") {
        subject.gameReject (me.id)

        it ("returns no messages") {
          meRep.expectNoMsg (100 millis)
          himRep.expectNoMsg (100 millis)
        }
      }
    }

    describe ("with its boardFactory replaced by a mock") {
      val board = mock (classOf[ConvolutionBoard])
      subject.boardFactory = {() => board}
      val meRep = TestProbe ()
      val me = DisplayPlayer (123, "Billy", 1234L, meRep.ref)
      val himRep = TestProbe ()
      val him = DisplayPlayer (234, "Teddy", 2345L, himRep.ref)
      subject.gamePrepare (me, him)
      subject.gameAccept (him.id)
      meRep.receiveOne (1 seconds) // throw away StartGame
      himRep.receiveOne (1 seconds) // throw away StartGame

      describe ("which is set up to be won with a tie") {
        when (board.isGameOver).thenReturn (true)

        describe ("and a move from the horizontal player comes after the game is over") {
          subject.acceptMove (123, 4)

          it ("returns no messages") {
            meRep.expectNoMsg (100 millis)
            himRep.expectNoMsg (100 millis)
          }
        }

        describe ("and a move from the vertical player comes after the game is over") {
          subject.acceptMove (234, 4)

          it ("returns no messages") {
            meRep.expectNoMsg (100 millis)
            himRep.expectNoMsg (100 millis)
          }
        }
      }

      describe ("which is set up to accept one more move and then be over") {
        when (board.isGameOver).thenReturn (false).thenReturn (true)
        when (board.move (anyInt (), anyInt ())).thenReturn (board)
        when (board.horizontalIsNext).thenReturn (Some (true))

        describe ("and such a move is accepted") {
          subject.acceptMove (123, 4)
          meRep.receiveOne (1 seconds) // throw away Turn
          himRep.receiveOne (1 seconds) // throw away Turn

          it ("the game has disappeared from the service") {
            assert (subject.findGame (123) === None)
            assert (subject.findGame (234) === None)
          }
        }
      }
    }

    system.terminate ()
  }

  describe ("A RejectGame message, converted to a JsValue") {
    val player = DisplayPlayer (321, "Mikey", 4321, null)
    val result = RejectGame (player).toJsValue

    it ("produces the proper structure") {
      assert (result.toString () === toJsValue (player).toString ())
    }
  }

  describe ("A StartGame message, converted to a JsValue") {
    val player = DisplayPlayer (321, "Mikey", 4321, null)
    val board = ConvolutionBoard (8).build
    val result = StartGame (board, true, player).toJsValue

    it ("produces the proper structure") {
      assert (result.toString === JsObject (Seq (
        "board" -> toJsValue (board),
        "isHorizontal" -> JsBoolean (true),
        "opponent" -> toJsValue (player)
      )).toString)
    }
  }

  describe ("A Turn message, converted to a JsValue") {
    val board = ConvolutionBoard (8).build
    val result = Turn (board, true).toJsValue

    it ("produces the proper structure") {
      assert (result.toString === JsObject (Seq (
        "board" -> toJsValue (board),
        "yourTurn" -> JsBoolean (true)
      )).toString)
    }
  }
}
