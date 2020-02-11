package xox.server.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Source, Tcp}
import xox.server.config.ServerConfig
import xox.server.net.Client

import scala.concurrent.Future

object ClientSource {
  def apply(serverConfig: ServerConfig)
           (idSource: Source[String, _])
           (implicit system: ActorSystem): Source[Client, Future[ServerBinding]] =
    Tcp().bind(serverConfig.address.getHostString, serverConfig.address.getPort)
      .zip(idSource).map { case (connection, id) =>
        Client(id, connection.remoteAddress, connection.flow)
      }.log("Connecting Clients")
}
