package xox.server

import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import xox.server.codecs.{ClientCommandEncoder, ScodecClientCommandEncoder, ScodecServerCommandDecoder, ServerCommandDecoder}
import xox.server.config.Config
import xox.server.util.{IdGenerator, UUIDIdGenerator}
import zio.Task
import zio.interop.catz._

final case class Dependencies(config: Config,
                              idGenerator: IdGenerator,
                              serverCommandDecoder: ServerCommandDecoder,
                              clientCommandEncoder: ClientCommandEncoder)

object Dependencies {
  def create: Task[Dependencies] =
    Config.load().map { config =>
      val idGenerator = new UUIDIdGenerator
      val serverCommandDecoder = new ScodecServerCommandDecoder
      val clientCommandEncoder = new ScodecClientCommandEncoder
      Dependencies(
        config,
        idGenerator,
        serverCommandDecoder,
        clientCommandEncoder
      )
    }.tapBoth(
      error => logger.error(error)("Failed to set up dependencies"),
      _ => logger.debug("Dependencies have been set up")
    )

  private val logger = Slf4jLogger.getLogger[Task]
}
