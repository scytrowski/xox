package xox.server.stream

import java.net.InetSocketAddress

import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.concurrent.ScalaFutures
import xox.server.config.ServerConfig
import xox.server.fixture.{ClientFixture, StreamSpec}
import xox.server.mock.TestIdGenerator
import xox.server.net.Client

class ClientSourceTest extends StreamSpec("ClientSourceTest") with ClientFixture with ScalaFutures {
  "ClientSource" should {

    "emit connected clients" in {
      val config = ServerConfig(InetSocketAddress.createUnresolved("127.0.0.1", 0))
      val ids = List("a", "b", "c", "d", "e")
      val idGenerator = new TestIdGenerator(ids:_*)

      val sink = TestSink.probe[Client]
      val (bindingFut, clientProbe) = ClientSource(config, idGenerator)
        .toMat(sink)(Keep.both)
        .run()

      whenReady(bindingFut) { binding =>
        withClients(binding.localAddress, ids.length) { clients =>
          ids.zip(clients).foreach { case (id, client) =>
            val connected = clientProbe.requestNext()
            connected.id mustBe id
            connected.address mustBe client.address
          }
        }
      }
    }

  }
}
