package xox.server.net

import xox.core.protocol.ServerCommand

final case class IncomingCommand(clientId: String, command: ServerCommand)
