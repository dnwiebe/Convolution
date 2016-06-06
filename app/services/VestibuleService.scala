package services

import com.google.inject.Singleton

import scala.collection.mutable.ListBuffer

/**
  * Created by dnwiebe on 6/5/16.
  */

case class DisplayPlayer (
  name: String
)

@Singleton
class VestibuleService {
  private val data = ListBuffer[DisplayPlayer] ()

  def waitingPlayers (): List[DisplayPlayer] = {
    data.toList
  }

  def playerEntered (player: DisplayPlayer): Unit = {
    data.append (player)
  }
}
