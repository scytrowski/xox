package xox.server.game

sealed abstract class Match {
  def id: String
  def ownerId: String
  def isInvolved(playerId: String): Boolean
}

object Match {
  final case class WaitingForOpponent(id: String, ownerId: String) extends Match {
    override def isInvolved(playerId: String): Boolean = ownerId == playerId

    def start(opponentId: String): Match = Ongoing(id, ownerId, opponentId)
  }

  final case class Ongoing(id: String, ownerId: String, opponentId: String) extends Match {
    override def isInvolved(playerId: String): Boolean =
      ownerId == playerId || opponentId == playerId
  }
}
