package xox.core.protocol

final case class ErrorCauseModel(code: Int, message: String)

sealed abstract class ErrorCause(val code: Int) {
  def message: String

  final def toModel: ErrorCauseModel = ErrorCauseModel(code, message)
}

object ErrorCause {
  final case class PlayerAlreadyLogged(nick: String) extends ErrorCause(1) {
    override val message: String =
      s"Player with nick $nick is already logged in"
  }
  final case class PlayerAlreadyInMatch(playerId: String, matchId: String)
      extends ErrorCause(2) {
    override val message: String =
      s"Player with ID $playerId is already in match with ID $matchId"
  }
  final case class MatchAlreadyStarted(matchId: String) extends ErrorCause(3) {
    override val message: String = s"Match with ID $matchId is already started"
  }
  final case class UnknownPlayer(playerId: String) extends ErrorCause(4) {
    override val message: String = s"Player with ID $playerId is unknown"
  }
  final case class UnknownMatch(matchId: String) extends ErrorCause(5) {
    override val message: String = s"Match with ID $matchId is unknown"
  }
}
