package xox.server.stream

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Source, Tcp}
import xox.server.config.ServerConfig
import xox.server.net.Client
import xox.server.util.IdGenerator

import scala.concurrent.Future

object ClientSource {
  import xox.server.syntax.akka.stream._

  def apply(serverConfig: ServerConfig, idGenerator: IdGenerator)(
      implicit system: ActorSystem
  ): Source[Client, Future[ServerBinding]] = {
    implicit val adapter: LoggingAdapter = system.log
    Tcp()
      .bind(serverConfig.address.getHostString, serverConfig.address.getPort)
      .map { connection =>
        val id = idGenerator.generate
        Client(id, connection.remoteAddress, connection.flow)
      }
      .infoLog("Connecting clients")
      .logOnError("Connecting clients")
  }
}
