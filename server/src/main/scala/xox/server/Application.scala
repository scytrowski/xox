package xox.server

import akka.actor.ActorSystem
import akka.io.{IO, Tcp}
import xox.server.config.Config
import xox.server.game.{MatchManagerActor, PlayerManagerActor}
import xox.server.handler.CommandManagerActor.CommandHandlerFactory
import xox.server.handler.{ClientManagerActor, CommandHandlerActor, CommandManagerActor}
import xox.server.net.ServerActor.ClientFactory
import xox.server.net.{ClientActor, ServerActor}
import xox.server.util.UuidIdGenerator

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Application extends App {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val system: ActorSystem = ActorSystem()
  val tcp = IO(Tcp)

  Config.load() match {
    case Success(config) =>
      val idGenerator = UuidIdGenerator
      val playerManager = system.actorOf(PlayerManagerActor.props(idGenerator), "player-manager")
      val matchManager = system.actorOf(MatchManagerActor.props(idGenerator), "match-manager")
      val commandHandlerFactory: CommandHandlerFactory = refFactory => refFactory.actorOf(CommandHandlerActor.props(playerManager, matchManager))
      val commandManager = system.actorOf(CommandManagerActor.props(config.handler, idGenerator, commandHandlerFactory), "command-manager")
      val clientManager = system.actorOf(ClientManagerActor.props(commandManager, playerManager), "client-manager")
      val clientFactory: ClientFactory = refFactory => (id, connection) => refFactory.actorOf(ClientActor.props(id, connection, clientManager), s"client-$id")
      system.actorOf(ServerActor.props(config.server, idGenerator, tcp, clientFactory), "server")
    case Failure(ex) =>
      system.log.error(ex, "Unable to load configuration")
  }

}
