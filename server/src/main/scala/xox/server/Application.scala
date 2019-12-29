package xox.server

import zio.{App, UIO, ZIO}

object Application extends App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    UIO(0)
}
