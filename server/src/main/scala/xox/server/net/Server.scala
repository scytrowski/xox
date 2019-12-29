package xox.server.net

import cats.effect.Blocker
import fs2.io.tcp.{Socket, SocketGroup}
import xox.server._
import xox.server.config.ServerConfig
import zio.interop.catz._
import zio.{Managed, Runtime, Task, UIO}

abstract class Server {
  type SClient <: Client

  def clients: MStream[SClient]
}

final class TcpServer private(socketStream: MStream[Socket[Task]],
                              clientGen: Socket[Task] => UIO[TcpClient]) extends Server {
  override type SClient = TcpClient

  override def clients: MStream[TcpClient] =
    socketStream.map(_.mapM(clientGen))
}

object TcpServer {
  def managed(config: ServerConfig,
              clientGen: Socket[Task] => UIO[TcpClient])(implicit rt: Runtime[Any]): Managed[Throwable, TcpServer] =
    for {
      blocker     <- Blocker[Task].toManaged
      socketGroup <- SocketGroup[Task](blocker).toManaged
    } yield new TcpServer(socketGroup.server[Task](config.address).map(_.toManaged), clientGen)
}
