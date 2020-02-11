package xox.server.fixture

import akka.stream.Materializer

abstract class StreamSpec(name: String) extends ActorSpec(name) {
  protected final implicit val materializer: Materializer = Materializer(system)
}
