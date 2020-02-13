package xox.server.fixture

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.TryValues
import xox.server.config.AppConfig

trait ConfigFixture extends TryValues {
  protected lazy val appConfig: AppConfig = AppConfig.load(config).success.value

  protected lazy val config: Config =
    ConfigFactory.load("application.integration.conf")
}
