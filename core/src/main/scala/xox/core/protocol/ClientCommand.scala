package xox.core.protocol

sealed abstract class ClientCommand

object ClientCommand {
  final case class LoginOk(playerId: String) extends ClientCommand
  final case class CreateMatchOk(matchId: String) extends ClientCommand
  final case class JoinMatchOk(opponentId: String) extends ClientCommand
  case object Timeout extends ClientCommand
  final case class Error(reason: String) extends ClientCommand
}
