package xox.server.net

import xox.core.protocol.ClientCommand

sealed abstract class OutgoingCommand {
  def command: ClientCommand

  final def isAddressedTo(clientId: String): Boolean =
    this match {
      case OutgoingCommand.Private(id, _) if id == clientId => true
      case OutgoingCommand.Broadcast(_)                     => true
      case _                                                => false
    }
}

object OutgoingCommand {
  final case class Private(clientId: String, command: ClientCommand)
      extends OutgoingCommand
  final case class Broadcast(command: ClientCommand) extends OutgoingCommand
}
