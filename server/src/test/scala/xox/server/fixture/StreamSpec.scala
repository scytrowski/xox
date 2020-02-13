package xox.server.fixture

import akka.stream.Materializer

abstract class StreamSpec(name: String) extends ActorSpec(name) {
  implicit final protected val materializer: Materializer = Materializer(system)
}
