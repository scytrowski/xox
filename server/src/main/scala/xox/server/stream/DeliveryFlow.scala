package xox.server.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}
import xox.server.net.OutgoingCommand

object DeliveryFlow {
  import xox.server.syntax.akka.stream._

  def apply()(
      implicit mat: Materializer
  ): Flow[OutgoingCommand, OutgoingCommand, NotUsed] = {
    implicit val adapter: LoggingAdapter = mat.system.log
    val (commandSink, commandSource) = MergeHub
      .source[OutgoingCommand]
      .toMat(BroadcastHub.sink[OutgoingCommand])(Keep.both)
      .run()
    Flow
      .fromSinkAndSource(commandSink, commandSource)
      .debugLog("Delivery")
  }
}
