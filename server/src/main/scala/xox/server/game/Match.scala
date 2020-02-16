package xox.server.game

import xox.core.game.{MatchInfo, MatchParameters}

sealed abstract class Match {
  def id: String
  def ownerId: String
  def isInvolved(playerId: String): Boolean
  def toInfo: MatchInfo
}

object Match {
  final case class WaitingForOpponent(
      id: String,
      ownerId: String,
      parameters: MatchParameters
  ) extends Match {
    override def isInvolved(playerId: String): Boolean = ownerId == playerId

    override def toInfo: MatchInfo = MatchInfo(id, ownerId, None, parameters)

    def start(
        opponentId: String
    )(stateFactory: MatchParameters => MatchState): Ongoing = {
      val state = stateFactory(parameters)
      Ongoing(id, ownerId, opponentId, state)
    }
  }

  final case class Ongoing(
      id: String,
      ownerId: String,
      opponentId: String,
      state: MatchState
  ) extends Match {
    override def isInvolved(playerId: String): Boolean =
      ownerId == playerId || opponentId == playerId

    override def toInfo: MatchInfo =
      MatchInfo(id, ownerId, Some(opponentId), state.parameters)
  }
}
