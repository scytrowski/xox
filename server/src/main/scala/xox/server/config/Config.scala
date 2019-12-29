package xox.server.config

import java.net.{InetAddress, InetSocketAddress}

import zio.{Task, UIO}

final case class Config(server: ServerConfig)

object Config {
  // fixme: Load config from resource file
  def load: Task[Config] =
    UIO {
      Config(
        ServerConfig(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6500))
      )
    }
}