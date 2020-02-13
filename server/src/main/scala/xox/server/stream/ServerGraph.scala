package xox.server.stream

import akka.stream.Materializer
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.net.{Client, IncomingCommand, OutgoingCommand}

import scala.concurrent.Future

object ServerGraph {
  def apply(clientSource: Source[Client, Future[ServerBinding]],
            decoderFlow: Flow[ByteString, ServerCommand, _],
            encoderFlow: Flow[ClientCommand, ByteString, _],
            handlerFlow: Flow[IncomingCommand, OutgoingCommand, _],
            deliveryFlow: Flow[OutgoingCommand, OutgoingCommand, _])(implicit mat: Materializer): RunnableGraph[Future[ServerBinding]] = {
    clientSource.toMat(Sink.foreach { client =>
      client.flow.join {
        val delivery = deliveryFlow
          .filter(_.isAddressedTo(client.id))
          .map(_.command)
        Flow[ByteString]
          .via(decoderFlow)
          .map(IncomingCommand(client.id, _))
          .via(handlerFlow)
          .via(delivery)
          .via(encoderFlow)
      }.run()
    })(Keep.left)
  }
}
