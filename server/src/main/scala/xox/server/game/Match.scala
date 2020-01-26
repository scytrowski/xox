package xox.server.game

import xox.core.game.MatchParameters

sealed abstract class Match {
  def id: String
  def ownerId: String
  def isInvolved(playerId: String): Boolean
}

object Match {
  final case class WaitingForOpponent(id: String, ownerId: String, parameters: MatchParameters) extends Match {
    override def isInvolved(playerId: String): Boolean = ownerId == playerId

    def start(opponentId: String): Ongoing = {
      val state = MatchState.create(parameters)
      Ongoing(id, ownerId, opponentId, state)
    }
  }

  final case class Ongoing(id: String, ownerId: String, opponentId: String, state: MatchState) extends Match {
    override def isInvolved(playerId: String): Boolean =
      ownerId == playerId || opponentId == playerId
  }
}
