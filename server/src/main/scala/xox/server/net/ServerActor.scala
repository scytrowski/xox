package xox.server.net

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.io.Tcp.{Bind, Bound, CommandFailed, Connected, Register}
import akka.io.{IO, Tcp}
import xox.server.config.ServerConfig
import xox.server.net.ServerActor.ClientFactory
import xox.server.util.IdGenerator

final class ServerActor private(config: ServerConfig,
                                idGenerator: IdGenerator,
                                clientFactory: ClientFactory) extends Actor with ActorLogging {
  import context.system

  IO(Tcp) ! Bind(self, config.address)

  override val receive: Receive = awaitBound

  private def awaitBound: Receive = {
    case _: Bound =>
      log.info(s"Server started on ${config.address}")
      context become awaitConnected
    case CommandFailed(_: Bind) =>
      // fixme: Error handling
      context.stop(self)
  }

  private def awaitConnected: Receive = {
    case Connected(remote, _) =>
      val id = idGenerator.generate
      log.info(s"Accepted new client's $id connection from $remote")
      val connection = sender()
      val client = clientFactory(context)(id, connection)
      connection ! Register(client)
  }
}

object ServerActor {
  type ClientFactory = ActorRefFactory => (String, ActorRef) => ActorRef

  def props(config: ServerConfig, idGenerator: IdGenerator, clientFactory: ClientFactory): Props =
    Props(new ServerActor(config, idGenerator, clientFactory))
}
