package xox.server.stream

import java.net.InetSocketAddress

import akka.stream.scaladsl.{Keep, Sink, Source}
import org.scalatest.concurrent.ScalaFutures
import xox.server.config.ServerConfig
import xox.server.fixture.{SocketFixture, StreamSpec}

class ClientSourceTest extends StreamSpec("ClientSourceTest") with SocketFixture with ScalaFutures {
  "ClientSource" should {

    "emit connected clients" in {
      val config = ServerConfig(InetSocketAddress.createUnresolved("127.0.0.1", 0))
      val ids = List("a", "b", "c", "d", "e")
      val idSource = Source(ids)

      val (bindingFut, resultFut) = ClientSource(config)(idSource)
        .toMat(Sink.seq)(Keep.both)
        .run()

      whenReady(bindingFut) { binding =>
        withConnections(binding.localAddress, ids.length) { sockets =>
          whenReady(binding.unbind()) { _ =>
            resultFut.futureValue
              .map(c => c.id -> c.address) must contain theSameElementsInOrderAs ids.zip(sockets.map(_.address))
          }
        }
      }
    }

  }
}
