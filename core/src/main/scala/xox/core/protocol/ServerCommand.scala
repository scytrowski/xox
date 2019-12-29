package xox.core.protocol

sealed abstract class ServerCommand {
  def clientId: String
}

object ServerCommand {
  final case class Login(clientId: String, nick: String) extends ServerCommand
}
