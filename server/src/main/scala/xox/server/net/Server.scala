package xox.server.net

import java.net.InetSocketAddress

import cats.effect.{Blocker, Concurrent, ContextShift, Resource}
import fs2.io.tcp.{Socket, SocketGroup}
import xox.server.config.ServerConfig
import xox.server.util.IdGenerator

object GameServer {
  def resource[F[_]: Concurrent: ContextShift](config: ServerConfig): Resource[F, GameServer[F]] =
    Blocker[F].flatMap { blocker =>
      Server.resource(config.address, blocker, GameClient.create[F])
    }
}

trait Server[F[_], I, O] {
  def connections: fs2.Stream[F, Client[F, I, O]]
}

object Server {
  def resource[F[_]: Concurrent: ContextShift, I, O](address: InetSocketAddress,
                                                     blocker: Blocker,
                                                     clientGen: (String, Socket[F]) => F[Client[F, I, O]]): Resource[F, Server[F, I, O]] =
    SocketGroup(blocker).map { socketGroup =>
      new Server[F, I, O] {
        final override val connections: fs2.Stream[F, Client[F, I, O]] =
          socketGroup.server(address)
            .flatMap(fs2.Stream.resource)
            .zip(IdGenerator.stream)
            .evalMap { case (socket, id) => clientGen(id, socket) }
      }
    }
}
