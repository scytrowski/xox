package xox.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Tcp.ServerBinding
import xox.server.config.AppConfig
import xox.server.game.MatchStateFactoryLive
import xox.server.handler.CommandHandlerLive
import xox.server.stream.{ClientSource, DecoderFlow, EncoderFlow, HandlerFlow, IdSource, ServerGraph}
import xox.server.util.UuidIdGenerator

import scala.concurrent.Future

object ServerInitializer {
  def initialize(config: AppConfig)(implicit mat: Materializer): RunnableGraph[Future[ServerBinding]] = {
    implicit val system: ActorSystem = mat.system

    val idGenerator = UuidIdGenerator
    val matchStateFactory = new MatchStateFactoryLive

    val state = ServerStateLive(Map.empty, Map.empty, idGenerator, matchStateFactory)
    val handler = new CommandHandlerLive(idGenerator)

    val idSource = IdSource(idGenerator)
    val connectionSource = ClientSource(config.server)(idSource)
    val decoderFlow = DecoderFlow(config.protocol)
    val encoderFlow = EncoderFlow(config.protocol)
    val handlerFlow = HandlerFlow(handler, state)
    ServerGraph(connectionSource, decoderFlow, encoderFlow, handlerFlow)
  }
}
