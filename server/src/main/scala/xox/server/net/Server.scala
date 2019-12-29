package xox.server.net

import xox.server._
import java.net.InetSocketAddress

import cats.effect.{Blocker, Resource}
import fs2.io.tcp.{Socket, SocketGroup}
import zio.{Task, UIO}
import zio.interop.catz._

abstract class Server {
  type SClient <: Client

  def clients: RStream[SClient]
}

final class TcpServer private(socketStream: RStream[Socket[Task]],
                              clientGen: Socket[Task] => UIO[TcpClient]) extends Server {
  override type SClient = TcpClient

  override def clients: RStream[TcpClient] =
    socketStream.map(_.evalMap(clientGen))
}

object TcpServer {
  def resource(address: InetSocketAddress,
               clientGen: Socket[Task] => UIO[TcpClient]): Resource[Task, TcpServer] =
    for {
      blocker     <- Blocker[Task]
      socketGroup <- SocketGroup[Task](blocker)
    } yield new TcpServer(socketGroup.server(address), clientGen)
}
