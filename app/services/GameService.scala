package services

import models.ConvolutionBoard

/**
  * Created by dnwiebe on 6/3/16.
  */
class GameService {
  private var nextId = 1000
  private val data = scala.collection.mutable.Map[String, ConvolutionBoard] ()

  def newGame (order: Int): (String, ConvolutionBoard) = {
    val board = ConvolutionBoard (order).randomize ().build
    val id = nextId.toString
    nextId += 1
    data.put (id, board)
    (id, board)
  }

  def findGame (id: String): Option[ConvolutionBoard] = {
    data.get (id)
  }

  def updateGame (id: String, board: ConvolutionBoard): Unit = {
    data.put (id, board)
  }

  def removeGame (id: String): Unit = {
    data.remove (id)
  }
}
