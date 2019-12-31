package xox.server

import xox.core.protocol.{ClientCommand, ServerCommand}

package object net {
  type GameServer[F[_]] = Server[F, ServerCommand, ClientCommand]

  type GameClient[F[_]] = Client[F, ServerCommand, ClientCommand]
}
