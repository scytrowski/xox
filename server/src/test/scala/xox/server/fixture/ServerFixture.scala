package xox.server.fixture

import akka.stream.scaladsl.Tcp.ServerBinding
import xox.server.ServerInitializer
import xox.server.config.AppConfig
import xox.server.mock.TestIdGenerator

trait ServerFixture { self: StreamSpec =>
  def withServer[U](config: AppConfig, ids: String*)(
      f: ServerBinding => U
  ): Unit =
    whenReady(
      ServerInitializer.initialize(config, new TestIdGenerator(ids: _*)).run()
    ) { binding =>
      try {
        f(binding)
      } finally {
        binding.unbind().futureValue
      }
    }
}
