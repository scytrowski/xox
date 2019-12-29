package xox.core.protocol

sealed trait ClientCommand {

}

object ClientCommand {
  final case class LoginOk(playerId: String) extends ClientCommand
}
