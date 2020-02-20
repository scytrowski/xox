package xox.core.protocol

import xox.core.game.MatchParameters

sealed abstract class ServerCommand

object ServerCommand {
  case object RequestPlayerList             extends ServerCommand
  final case class Login(nick: String)      extends ServerCommand
  final case class Logout(playerId: String) extends ServerCommand
  case object RequestMatchList              extends ServerCommand
  final case class CreateMatch(playerId: String, parameters: MatchParameters)
      extends ServerCommand
  final case class JoinMatch(playerId: String, matchId: String)
      extends ServerCommand
  final case class MakeTurn(playerId: String, x: Int, y: Int)
      extends ServerCommand
}
