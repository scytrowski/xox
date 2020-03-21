package xox.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Tcp.ServerBinding
import xox.core.codec.{ClientCommandCodec, ServerCommandCodec}
import xox.core.stream.{DecoderFlow, DeliveryFlow, EncoderFlow, FramingProtocol}
import xox.server.config.AppConfig
import xox.server.game.MatchStateFactoryLive
import xox.server.handler.CommandHandlerLive
import xox.server.net.OutgoingCommand
import xox.server.stream._
import xox.server.util.IdGenerator

import scala.concurrent.Future

object ServerInitializer {
  def initialize(config: AppConfig, idGenerator: IdGenerator)(
      implicit mat: Materializer
  ): RunnableGraph[Future[ServerBinding]] = {
    implicit val system: ActorSystem = mat.system

    val matchStateFactory = new MatchStateFactoryLive

    val state =
      ServerStateLive(Map.empty, Map.empty, idGenerator, matchStateFactory)
    val handler = new CommandHandlerLive(idGenerator)
    val framing = FramingProtocol.simple(1024)

    val connectionSource = ClientSource(config.server, idGenerator)
    val decoderFlow      = DecoderFlow(ServerCommandCodec.decoder, framing)
    val encoderFlow      = EncoderFlow(ClientCommandCodec.encoder, framing)
    val handlerFlow      = HandlerFlow(handler, state)
    val deliveryFlow     = DeliveryFlow[OutgoingCommand]()
    ServerGraph(
      connectionSource,
      decoderFlow,
      encoderFlow,
      handlerFlow,
      deliveryFlow
    )
  }
}
