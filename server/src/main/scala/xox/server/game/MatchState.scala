package xox.server.game

import xox.core.game.{Mark, MatchParameters}
import xox.server.game.BoardLike.SetResult
import xox.server.game.MatchState.PutMarkResult

final case class MatchState(
    parameters: MatchParameters,
    ownerMark: Mark,
    turnMark: Mark,
    board: BoardLike
) {
  def opponentMark: Mark = ownerMark.opposite

  def putMark(x: Int, y: Int, mark: Mark): PutMarkResult =
    if (turnMark == mark) {
      board.set(x, y, mark) match {
        case SetResult.Ok(updatedBoard) =>
          val updatedState = copy(
            turnMark = turnMark.opposite,
            board = updatedBoard
          )
          PutMarkResult.Ok(updatedState, updatedBoard.freeLeft)
        case SetResult.Victory => PutMarkResult.Victory
        case SetResult.Draw    => PutMarkResult.Draw
        case SetResult.AlreadyTaken | SetResult.OutOfBounds =>
          PutMarkResult.IncorrectField
      }
    } else
      PutMarkResult.NotYourTurn
}

object MatchState {
  def create(parameters: MatchParameters): MatchState = {
    val ownerMark = Mark.random
    val turnMark  = Mark.random
    val board     = Board.create(parameters.boardSize)
    new MatchState(parameters, ownerMark, turnMark, board)
  }

  sealed abstract class PutMarkResult

  object PutMarkResult {
    final case class Ok(state: MatchState, fieldsLeft: Int)
        extends PutMarkResult
    case object Victory        extends PutMarkResult
    case object Draw           extends PutMarkResult
    case object IncorrectField extends PutMarkResult
    case object NotYourTurn    extends PutMarkResult
  }
}
