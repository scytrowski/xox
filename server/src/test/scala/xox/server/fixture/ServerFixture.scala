package xox.server.fixture

import akka.stream.scaladsl.Tcp.ServerBinding
import org.scalatest.concurrent.ScalaFutures
import xox.server.ServerInitializer
import xox.server.config.AppConfig

trait ServerFixture extends ScalaFutures { self: StreamSpec =>
  def withServer[U](config: AppConfig)(f: ServerBinding => U): Unit =
    whenReady(ServerInitializer.initialize(config).run()) { binding =>
      try {
        f(binding)
      } finally {
        binding.unbind().futureValue
      }
    }
}
