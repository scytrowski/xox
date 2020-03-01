package xox.api.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try
import io.circe.config.syntax._

final case class AppConfig(api: ApiConfig, server: ServerConfig)

object AppConfig {
  import xox.core.config.ConfigDecoders._
  import io.circe.generic.auto._

  def load(tsConfig: Config = ConfigFactory.load()): Try[AppConfig] =
    tsConfig.as[AppConfig]("xox").toTry
}
