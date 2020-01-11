package xox.server.config

import scala.concurrent.duration.FiniteDuration

final case class HandlerConfig(timeout: FiniteDuration)
