package xox.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import xox.server.config.AppConfig

import scala.util.{Failure, Success}

object Application extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)

  AppConfig.load() match {
    case Success(config) =>
      val processFuture = ServerInitializer.initialize(config).run()

      sys.addShutdownHook {
        processFuture.flatMap(_.unbind())(system.dispatcher)
      }
    case Failure(error)  =>
      system.log.error(error, "Unable to load configuration")
  }
}
