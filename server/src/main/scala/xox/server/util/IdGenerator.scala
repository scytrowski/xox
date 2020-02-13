package xox.server.util

import java.util.UUID

trait IdGenerator {
  def generate: String
}

object UuidIdGenerator extends IdGenerator {
  override def generate: String = UUID.randomUUID().toString
}
