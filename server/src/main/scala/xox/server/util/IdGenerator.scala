package xox.server.util

import java.util.UUID

import cats.syntax.applicative._
import cats.{Applicative, Defer}

object IdGenerator {
  def stream[F[_]: Applicative: Defer]: fs2.Stream[F, String] =
    fs2.Stream.repeatEval(Defer[F].defer(single))

  def single[F[_]: Applicative]: F[String] =
    UUID.randomUUID()
      .toString
      .pure
}
