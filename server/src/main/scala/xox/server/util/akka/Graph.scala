package xox.server.util.akka

import akka.NotUsed
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source}

object Graph {
  // https://github.com/akka/akka/issues/24853#issuecomment-388616085
  def sinkToSource[T]: RunnableGraph[(Sink[T, NotUsed], Source[T, NotUsed])] =
    Source.asSubscriber[T]
    .toMat(Sink.asPublisher[T](fanout = false))(Keep.both)
    .mapMaterializedValue {
      case (sub, pub) => (Sink.fromSubscriber(sub), Source.fromPublisher(pub))
    }
}
