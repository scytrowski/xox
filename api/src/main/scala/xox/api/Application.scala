package xox.api

import akka.actor.ActorSystem
import akka.stream.Materializer
import xox.api.config.AppConfig

import scala.util.{Failure, Success}

object Application extends App {
  implicit val system: ActorSystem        = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)

  AppConfig.load() match {
    case Success(config) =>
    case Failure(error) =>
      system.log.error(error, "Unable to load configuration")
  }
}
