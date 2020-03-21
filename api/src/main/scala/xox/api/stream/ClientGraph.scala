package xox.api.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.OutgoingConnection
import akka.stream.scaladsl.{Flow, RunnableGraph, Tcp}
import akka.util.ByteString
import xox.api.config.ServerConfig
import xox.core.protocol.{ClientCommand, ServerCommand}

import scala.concurrent.Future

object ClientGraph {
  def apply(config: ServerConfig)
           (decoderFlow: Flow[ByteString, ClientCommand, NotUsed],
            encoderFlow: Flow[ServerCommand, ByteString, NotUsed],
            deliveryFlow: Flow[ClientCommand, ServerCommand, NotUsed])
           (implicit system: ActorSystem): RunnableGraph[Future[OutgoingConnection]] =
    Tcp().outgoingConnection(config.address).join {
      Flow[ByteString]
        .via(decoderFlow)
        .via(deliveryFlow)
        .via(encoderFlow)
    }
}
