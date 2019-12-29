package xox.server

import xox.server.codecs.{ClientCommandEncoder, ScodecClientCommandEncoder, ScodecServerCommandDecoder, ServerCommandDecoder}
import xox.server.config.Config
import xox.server.util.{IdGenerator, UUIDIdGenerator}
import zio.Task

final case class Dependencies(config: Config,
                              idGenerator: IdGenerator,
                              serverCommandDecoder: ServerCommandDecoder,
                              clientCommandEncoder: ClientCommandEncoder)

object Dependencies {
  def create: Task[Dependencies] = {
    val idGenerator = new UUIDIdGenerator
    val serverCommandDecoder = new ScodecServerCommandDecoder
    val clientCommandEncoder = new ScodecClientCommandEncoder
    Config.load.map { config =>
      Dependencies(
        config,
        idGenerator,
        serverCommandDecoder,
        clientCommandEncoder
      )
    }
  }
}
