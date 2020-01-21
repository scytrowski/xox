package xox.core.protocol

sealed abstract class ServerCommand

object ServerCommand {
  final case class Login(nick: String) extends ServerCommand
  final case class CreateMatch(playerId: String) extends ServerCommand
  final case class JoinMatch(playerId: String, matchId: String) extends ServerCommand
}
