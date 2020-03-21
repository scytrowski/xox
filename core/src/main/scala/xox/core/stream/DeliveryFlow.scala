package xox.core.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}

object DeliveryFlow {
  import xox.core.syntax.akka.stream._

  def apply[T]()(implicit mat: Materializer): Flow[T, T, NotUsed] = {
    implicit val log: LoggingAdapter = mat.system.log
    val (sink, source) = MergeHub
      .source[T]
      .toMat(BroadcastHub.sink[T])(Keep.both)
      .run()
    Flow
      .fromSinkAndSourceCoupled(sink, source)
      .debugLog("Delivery")
  }
}
