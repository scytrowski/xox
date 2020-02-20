package xox.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import xox.server.config.AppConfig
import xox.server.util.UuidIdGenerator

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Application extends App {
  implicit val system: ActorSystem        = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext       = system.dispatcher

  AppConfig.load() match {
    case Success(config) =>
      val processFuture = ServerInitializer
        .initialize(config, UuidIdGenerator)
        .run()
        .andThen {
          case Success(binding) =>
            system.log.info(s"Server started on ${binding.localAddress}")
        }

      sys.addShutdownHook {
        processFuture.flatMap(_.unbind())
      }
    case Failure(error) =>
      system.log.error(error, "Unable to load configuration")
  }
}
