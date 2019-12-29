package xox.server.util

import java.util.UUID

import zio.UIO

trait IdGenerator {
  def generate: UIO[String]
}

final class UUIDIdGenerator extends IdGenerator {
  override def generate: UIO[String] = UIO(UUID.randomUUID().toString)
}
