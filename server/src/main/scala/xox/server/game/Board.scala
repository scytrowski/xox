package xox.server.game

import xox.core.game.Mark
import xox.server.game.Field.Empty

trait BoardLike {
  def get(x: Int, y: Int): Option[Field]

  def set(x: Int, y: Int, mark: Mark): SetResult
}

sealed abstract class SetResult extends BoardLike {
  override def get(x: Int, y: Int): Option[Field] =
    Some(this).collect {
      case SetResult.Ok(updatedBoard) => updatedBoard.get(x, y)
    }.flatten

  override def set(x: Int, y: Int, mark: Mark): SetResult =
    this match {
      case SetResult.Ok(updatedBoard) => updatedBoard.set(x, y, mark)
      case other                      => other
    }
}

object SetResult {
  final case class Ok(updatedBoard: Board) extends SetResult
  case object Victory                      extends SetResult
  case object Draw                         extends SetResult
  case object AlreadyTaken                 extends SetResult
  case object OutOfBounds                  extends SetResult
}

final case class Board private (
    size: Int,
    private val fields: List[Field],
    private val free: Int
) extends BoardLike {
  override def get(x: Int, y: Int): Option[Field] =
    Some(x -> y)
      .filter { case (x, y) => x >= 0 && x < size && y >= 0 && y < size }
      .map(index _ tupled)
      .flatMap(fields.lift)

  override def set(x: Int, y: Int, mark: Mark): SetResult =
    get(x, y) match {
      case Some(Empty) =>
        if (checkIfWin(x, y, mark))
          SetResult.Victory
        else if (free <= 1) {
          SetResult.Draw
        } else {
          val updatedFields = fields.updated(index(x, y), Field.Taken(mark))
          SetResult.Ok(copy(fields = updatedFields, free = free - 1))
        }
      case Some(_) => SetResult.AlreadyTaken
      case None    => SetResult.OutOfBounds
    }

  private def checkIfWin(x: Int, y: Int, mark: Mark): Boolean = {
    val inRow          = winInRow(x, y, mark)
    val inColumn       = winInColumn(x, y, mark)
    val inMainDiagonal = winInMainDiagonal(x, y, mark)
    val inSideDiagonal = winInSideDiagonal(x, y, mark)
    inRow || inColumn || inMainDiagonal || inSideDiagonal
  }

  private def winInRow(x: Int, y: Int, mark: Mark): Boolean =
    0.until(size)
      .filterNot(_ == x)
      .map(index(_, y))
      .map(fields(_))
      .forall(_ == Field.Taken(mark))

  private def winInColumn(x: Int, y: Int, mark: Mark): Boolean =
    0.until(size)
      .filterNot(_ == y)
      .map(index(x, _))
      .map(fields(_))
      .forall(_ == Field.Taken(mark))

  private def winInMainDiagonal(x: Int, y: Int, mark: Mark): Boolean = {
    val fieldIndex = index(x, y)
    0.until(size)
      .map(i => index(i, i))
      .filterNot(_ == fieldIndex)
      .map(fields(_))
      .forall(_ == Field.Taken(mark))
  }

  private def winInSideDiagonal(x: Int, y: Int, mark: Mark): Boolean = {
    val fieldIndex = index(x, y)
    0.until(size)
      .map(i => index(size - i - 1, i))
      .filterNot(_ == fieldIndex)
      .map(fields(_))
      .forall(_ == Field.Taken(mark))
  }

  private def index(x: Int, y: Int): Int = y * size + x
}

object Board {
  def create(size: Int): Board = {
    val free   = size * size
    val fields = List.fill[Field](free)(Field.Empty)
    new Board(size, fields, free)
  }
}
