package xox.server.game

import xox.core.game.{Mark, MatchParameters}

final case class MatchState(ownerMark: Mark, board: Board) {
  def opponentMark: Mark = ownerMark.opposite
}

object MatchState {
  def create(parameters: MatchParameters): MatchState = {
    val ownerMark = Mark.random
    val board     = Board.create(parameters.boardSize)
    new MatchState(ownerMark, board)
  }
}
