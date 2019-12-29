package xox.server.net

import fs2.io.tcp.Socket
import xox.server.Dependencies
import zio.{Managed, Task, UIO, Runtime}

object ServerInitializer {
  def initialize(dependencies: Dependencies)(implicit rt: Runtime[Any]): Managed[Throwable, Server] = {
    val clientGen = clientGenerator(dependencies)
    TcpServer.managed(dependencies.config.server, clientGen)
  }

  private def clientGenerator(dependencies: Dependencies): Socket[Task] => UIO[TcpClient] =
    socket =>
      dependencies.idGenerator.generate.map { id =>
        new TcpClient(id, socket, dependencies.serverCommandDecoder, dependencies.clientCommandEncoder)
      }
}
