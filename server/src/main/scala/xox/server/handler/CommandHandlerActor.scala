package xox.server.handler

import akka.actor.{Actor, ActorLogging, Props}
import xox.core.protocol.ServerCommand
import xox.server.handler.CommandHandlerActor.HandleCommand

final class CommandHandlerActor private extends Actor with ActorLogging {
  override def receive: Receive = {
    case HandleCommand(clientId, command) =>
      // fixme: Handle commands
  }
}

object CommandHandlerActor {
  final case class HandleCommand(clientId: String, command: ServerCommand)

  def props: Props = Props(new CommandHandlerActor)
}
