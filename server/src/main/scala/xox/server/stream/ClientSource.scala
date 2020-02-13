package xox.server.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Source, Tcp}
import xox.server.config.ServerConfig
import xox.server.net.Client
import xox.server.util.IdGenerator

import scala.concurrent.Future

object ClientSource {
  def apply(serverConfig: ServerConfig, idGenerator: IdGenerator)(
      implicit system: ActorSystem
  ): Source[Client, Future[ServerBinding]] =
    Tcp()
      .bind(serverConfig.address.getHostString, serverConfig.address.getPort)
      .map { connection =>
        val id = idGenerator.generate
        Client(id, connection.remoteAddress, connection.flow)
      }
      .log("Connecting Clients")
}
