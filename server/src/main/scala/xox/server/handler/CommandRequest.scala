package xox.server.handler

import akka.actor.ActorRef
import xox.core.protocol.ServerCommand

final case class CommandRequest(clientId: String,
                                command: ServerCommand,
                                recipient: ActorRef)
