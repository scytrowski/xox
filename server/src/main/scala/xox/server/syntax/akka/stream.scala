package xox.server.syntax.akka

import akka.stream.scaladsl.Flow

import scala.collection.immutable.{Iterable => ImmutableIterable}

object stream {
  implicit class FlowExtensions[In, Out, Mat](flow: Flow[In, Out, Mat]) {
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
