package xox.server.syntax.akka

import akka.event.LoggingAdapter
import akka.stream.scaladsl.{Flow, Source}
import xox.server.stream.LoggingFlow

import scala.collection.immutable.{Iterable => ImmutableIterable}

object stream {
  implicit class LoggingExtensionsForSource[Out, Mat](
      source: Source[Out, Mat]
  ) {
    def logInfo(logName: String, select: Out => String = _.toString)(
        implicit adapter: LoggingAdapter
    ): Source[Out, Mat] =
      source.via(LoggingFlow.info(logName)(select))

    def logDebug(logName: String, select: Out => String = _.toString)(
        implicit adapter: LoggingAdapter
    ): Source[Out, Mat] =
      source.via(LoggingFlow.debug(logName)(select))
  }

  implicit class LoggingExtensionsForFlow[In, Out, Mat](
      flow: Flow[In, Out, Mat]
  ) {
    def logInfo(logName: String, select: Out => String = _.toString)(
        implicit adapter: LoggingAdapter
    ): Flow[In, Out, Mat] =
      flow.via(LoggingFlow.info(logName)(select))

    def logDebug(logName: String, select: Out => String = _.toString)(
        implicit adapter: LoggingAdapter
    ): Flow[In, Out, Mat] =
      flow.via(LoggingFlow.debug(logName)(select))
  }

  implicit class PureStatefulMapConcat[In, Out, Mat](flow: Flow[In, Out, Mat]) {
    def pureStatefulMapConcat[S, T](
        initialState: S
    )(f: (S, Out) => (S, ImmutableIterable[T])): Flow[In, T, Mat] =
      flow.statefulMapConcat[T] {
        var state = initialState
        () =>
          t => {
            val (newState, elements) = f(state, t)
            state = newState
            elements
          }
      }
  }
}
