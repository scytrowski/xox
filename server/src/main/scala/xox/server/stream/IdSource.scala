package xox.server.stream

import akka.NotUsed
import akka.stream.scaladsl.Source
import xox.server.util.IdGenerator

object IdSource {
  def apply(idGenerator: IdGenerator): Source[String, NotUsed] =
    Source.fromIterator(() => Iterator.continually(idGenerator.generate))
}
