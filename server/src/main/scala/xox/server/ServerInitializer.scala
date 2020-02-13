package xox.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Tcp.ServerBinding
import xox.server.config.AppConfig
import xox.server.game.MatchStateFactoryLive
import xox.server.handler.CommandHandlerLive
import xox.server.stream._
import xox.server.util.IdGenerator

import scala.concurrent.Future

object ServerInitializer {
  def initialize(config: AppConfig, idGenerator: IdGenerator)(implicit mat: Materializer): RunnableGraph[Future[ServerBinding]] = {
    implicit val system: ActorSystem = mat.system

    val matchStateFactory = new MatchStateFactoryLive

    val state = ServerStateLive(Map.empty, Map.empty, idGenerator, matchStateFactory)
    val handler = new CommandHandlerLive(idGenerator)

    val connectionSource = ClientSource(config.server, idGenerator)
    val decoderFlow = DecoderFlow(config.protocol)
    val encoderFlow = EncoderFlow(config.protocol)
    val handlerFlow = HandlerFlow(handler, state)
    ServerGraph(connectionSource, decoderFlow, encoderFlow, handlerFlow)
  }
}
