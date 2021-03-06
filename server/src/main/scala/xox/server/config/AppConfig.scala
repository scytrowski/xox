package xox.server.config

import com.typesafe.config.{Config, ConfigFactory}
import io.circe.config.syntax._

import scala.util.Try

final case class AppConfig(server: ServerConfig, protocol: ProtocolConfig)

object AppConfig {
  import xox.core.config.ConfigDecoders._
  import io.circe.generic.auto._

  def load(tsConfig: Config = ConfigFactory.load()): Try[AppConfig] =
    tsConfig.as[AppConfig]("xox").toTry
}
