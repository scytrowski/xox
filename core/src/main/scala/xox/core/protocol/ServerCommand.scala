package xox.core.protocol

sealed abstract class ServerCommand

object ServerCommand {
  final case class Login(nick: String) extends ServerCommand
}
