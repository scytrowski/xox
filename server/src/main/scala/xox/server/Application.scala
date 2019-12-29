package xox.server

import zio.{App, ZIO, console}

object Application extends App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    runUnsafe.catchAll { error =>
      console.putStrLn(s"Error: ${error.getMessage}")
        .as(1)
    }

  private def runUnsafe: ZIO[zio.ZEnv, Throwable, Int] =
    ZIO.runtime[zio.ZEnv].flatMap { implicit rt =>
      for {
        dependencies <- Dependencies.create
      } yield 0
    }
}
