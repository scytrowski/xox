package xox.server.stream

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}
import xox.server.net.OutgoingCommand

object DeliveryFlow {
  def apply()(implicit mat: Materializer): Flow[OutgoingCommand, OutgoingCommand, NotUsed] = {
    val (commandSink, commandSource) = MergeHub.source[OutgoingCommand].toMat(BroadcastHub.sink[OutgoingCommand])(Keep.both).run()
    Flow.fromSinkAndSource(commandSink, commandSource)
  }
}
