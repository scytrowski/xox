package xox.server

import cats.syntax.functor._
import cats.effect.{Concurrent, ContextShift}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import xox.server.config.Config
import xox.server.net.GameServer
import zio.{App, Task, ZIO}
import zio.interop.catz._

object Application extends App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    runF[Task].catchAll { error =>
      zio.console.putStrLn(s"Unhandled error: ${error.getMessage}")
        .as(-1)
    }

  private def runF[F[_]: Concurrent: ContextShift]: F[Int] = {
    val logger = Slf4jLogger.getLogger[F]
    fs2.Stream.eval(Config.load[F]())
      .flatMap(config => fs2.Stream.resource(GameServer.resource[F](config.server)))
      .evalTap(_ => logger.info("Server started"))
      .flatMap(_.connections)
      .evalTap(client => logger.info(s"Accepted new client ${client.id} connection"))
      .compile
      .drain
      .as(0)
  }
}
