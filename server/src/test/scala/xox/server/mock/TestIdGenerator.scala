package xox.server.mock

import xox.server.util.IdGenerator

import scala.util.Random

final class TestIdGenerator(ids: String*) extends IdGenerator {
  override def generate: String =
    freeIds match {
      case freeId :: tail =>
        freeIds = tail
        freeId
      case Nil => Random.nextString(10)
    }

  private var freeIds: List[String] = ids.toList
}
