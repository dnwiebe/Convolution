package models

import scala.util.Random

/**
  * Created by dnwiebe on 6/1/16.
  */
trait ConvolutionBoard {
  def valueAt (column: Int, row: Int): Option[Int]
  def order: Int
  def horizontalScore: Int
  def verticalScore: Int
  def horizontalIsNext: Option[Boolean]
  def horizontalIsWinner: Option[Boolean]
  def fixedCoordinate: Int
  def isGameOver: Boolean
  def move (column: Int, row: Int): ConvolutionBoard
}

object ConvolutionBoard {

  def apply (order: Int): Builder = new Builder (order)
  def apply (original: ConvolutionBoard): Builder = new Builder (original)

  class Builder (val order: Int) {
    private val data = initializeData (order)
    private var _horizontalScore = 0
    private var _verticalScore = 0
    private var _isGameOver = false
    private var _horizontalIsNext: Option[Boolean] = Some (true)
    private var _horizontalIsWinner: Option[Boolean] = None
    private var _fixedCoordinate = 0

    def this (original: ConvolutionBoard) {
      this (original.order)
      for (column <- 0 until order; row <- 0 until order) {valueAt (row, column, original.valueAt (row, column))}
      _horizontalScore = original.horizontalScore
      _verticalScore = original.verticalScore
      _isGameOver = original.isGameOver
      _horizontalIsNext = original.horizontalIsNext
      _horizontalIsWinner = original.horizontalIsWinner
      _fixedCoordinate = original.fixedCoordinate
    }

    def randomize (): Builder = {
      randomize (data)
      this
    }

    def valueAt (column: Int, row: Int, value: Option[Int]): Builder = {
      data(column)(row) = value
      this
    }

    def horizontalScore (horizontalScore: Int): Builder = {
      _horizontalScore = horizontalScore
      this
    }

    def verticalScore (verticalScore: Int): Builder = {
      _verticalScore = verticalScore
      this
    }

    def isGameOver (isGameOver: Boolean): Builder = {
      _isGameOver = isGameOver
      this
    }

    def horizontalIsNext (horizontalIsNext: Option[Boolean]): Builder = {
      _horizontalIsNext = horizontalIsNext
      this
    }

    def horizontalIsWinner (horizontalIsWinner: Option[Boolean]): Builder = {
      _horizontalIsWinner = horizontalIsWinner
      this
    }

    def fixedCoordinate (fixedCoordinate: Int): Builder = {
      _fixedCoordinate = fixedCoordinate
      this
    }

    def build: ConvolutionBoard = RealConvolutionBoard (
      order,
      _horizontalScore,
      _verticalScore,
      _isGameOver,
      _horizontalIsNext,
      _horizontalIsWinner,
      _fixedCoordinate,
      data
    )

    private def initializeData (order: Int): Array[Array[Option[Int]]] = {
      val data = new Array[Array[Option[Int]]](order)
      (0 until order).foreach { col =>
        data (col) = new Array[Option[Int]](order)
        (0 until order).foreach { row =>
          data (col)(row) = None
        }
      }
      data
    }

    private def randomize (data: Array[Array[Option[Int]]]): Unit = {
      val numbers = (1 to (order * order)).toArray
      val rng = new Random (System.currentTimeMillis ())
      shuffle (numbers, rng)
      for (row <- 0 until order; col <- 0 until order) {
        data (col)(row) = Some (numbers((row * order) + col))
      }
    }

    private def shuffle (numbers: Array[Int], rng: Random): Unit = {
      for (from <- numbers.indices) {
        val to = rng.nextInt (numbers.length)
        val temp = numbers(to)
        numbers(to) = numbers(from)
        numbers(from) = temp
      }
    }
  }

  private case class RealConvolutionBoard (
    order: Int,
    horizontalScore: Int,
    verticalScore: Int,
    isGameOver: Boolean,
    horizontalIsNext: Option[Boolean],
    horizontalIsWinner: Option[Boolean],
    fixedCoordinate: Int,
    data: Array[Array[Option[Int]]]
  ) extends ConvolutionBoard {

    override def valueAt (column: Int, row: Int) = data(column)(row)

    override def move (column: Int, row: Int): ConvolutionBoard = {
      val builder = ConvolutionBoard (this)
      horizontalIsNext match {
        case None => throw new IllegalStateException ("No further moves are being accepted")
        case Some (true) => new HorizontalPlayerContext (this, builder, column, row).move ()
        case Some (false) => new VerticalPlayerContext (this, builder, column, row).move ()
      }
      builder.build
    }
  }

  private trait PlayerContext {
    val board: ConvolutionBoard
    val builder: Builder
    val column: Int
    val row: Int
    def validateFixedCoordinate ()
    def updateScore (moveScore: Int): Builder
    def chooseNextFixedCoordinate (): Int
    def assessGame (): Boolean

    def validateTarget (): Unit = {
      if (board.valueAt (column, row).isEmpty) {
        throw new IllegalArgumentException (s"Cannot move at ($column, $row): already empty")
      }
    }

    def removeNumber (): Unit = builder.valueAt (column, row, None)

    def chooseNextPlayer (): Option[Boolean] = {
      (assessGame (), board.horizontalIsNext) match {
        case (true, _) => None
        case (false, Some (x)) => Some (!x)
        case (false, None) => throw new UnsupportedOperationException ("Test-drive me!")
      }
    }

    def move (): Builder = {
      validateTarget ()
      validateFixedCoordinate ()
      updateScore (board.valueAt (column, row).get)
        .valueAt (column, row, None)
        .fixedCoordinate (chooseNextFixedCoordinate ())
        .isGameOver (assessGame ())
        .horizontalIsNext (chooseNextPlayer ())
    }

    protected def validateFixedCoordinate (player: String, looseCoordinate: Int, coordinateType: String): Unit = {
      if (looseCoordinate == board.fixedCoordinate) {return}
      throw new IllegalArgumentException (
        s"$player player must choose from $coordinateType ${board.fixedCoordinate}, not ($column, $row)"
      )
    }
  }

  private class HorizontalPlayerContext (val board: ConvolutionBoard, val builder: Builder,
                                         val column: Int, val row: Int) extends PlayerContext {
    override def validateFixedCoordinate () = validateFixedCoordinate ("Horizontal", row, "row")
    override def updateScore (moveScore: Int): Builder = {builder.horizontalScore (board.horizontalScore + moveScore)}
    override def chooseNextFixedCoordinate () = column
    override def assessGame (): Boolean = isColumnEmptyExcept (board, column, row)

    private def isColumnEmptyExcept (board: ConvolutionBoard, column: Int, row: Int): Boolean = {
      (0 until board.order).forall {i => (i == row) || board.valueAt (column, i).isEmpty}
    }
  }

  private class VerticalPlayerContext (val board: ConvolutionBoard, val builder: Builder,
                                       val column: Int, val row: Int) extends PlayerContext {
    override def validateFixedCoordinate () = validateFixedCoordinate ("Vertical", column, "column")
    override def updateScore (moveScore: Int): Builder = {builder.verticalScore (board.verticalScore + moveScore)}
    override def chooseNextFixedCoordinate () = row
    override def assessGame (): Boolean = isRowEmptyExcept (board, column, row)

    private def isRowEmptyExcept (board: ConvolutionBoard, column: Int, row: Int): Boolean = {
      (0 until board.order).forall {i => (i == column) || board.valueAt (i, row).isEmpty}
    }
  }
}
