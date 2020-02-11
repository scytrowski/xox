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
            handlerFlow: Flow[IncomingCommand, OutgoingCommand, _])(implicit mat: Materializer): RunnableGraph[Future[ServerBinding]] =
    clientSource.toMat(Sink.foreach { client =>
      client.flow.join {
        Flow[ByteString]
          .via(decoderFlow)
          .map(IncomingCommand(client.id, _))
          .via(handlerFlow)
          .collect { case out: OutgoingCommand if out.isAddressedTo(client.id) => out.command }
          .via(encoderFlow)
      }.run()
    })(Keep.left)
}
