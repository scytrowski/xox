package xox.core.protocol

import xox.core.game.{Mark, MatchInfo, MatchParameters}

sealed abstract class ClientCommand

object ClientCommand {
  final case class LoginOk(playerId: String) extends ClientCommand
  final case class PlayerLogged(playerId: String, nick: String) extends ClientCommand
  final case class CreateMatchOk(matchId: String, ownerId: String) extends ClientCommand
  final case class JoinMatchOk(matchId: String, opponentId: String, ownerMark: Mark) extends ClientCommand
  final case class MatchCreated(matchId: String, ownerId: String, parameters: MatchParameters) extends ClientCommand
  final case class MatchStarted(matchId: String, opponentId: String, ownerMark: Mark) extends ClientCommand
  final case class MatchFinished(matchId: String, winnerId: Option[String]) extends ClientCommand
  final case class MatchList(matches: List[MatchInfo]) extends ClientCommand
  case object Timeout extends ClientCommand
  final case class Error(reason: String) extends ClientCommand
}
