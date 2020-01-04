package xox.server.net

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.io.Tcp.{Bind, Bound, Connected, Register}
import akka.testkit.TestProbe
import xox.server.ActorSpec
import xox.server.config.ServerConfig
import xox.server.net.ServerActor.ClientFactory
import xox.server.util.IdGenerator

class ServerActorTest extends ActorSpec("ServerActorTest") {
  "ServerActor" should {

    "send Bind when created" in {
      val address = new InetSocketAddress("127.0.0.1", 6500)
      val tcp = TestProbe()

      val server = system.actorOf(ServerActor.props(testConfig(address), staticIdGenerator(), tcp.ref, staticClientFactory()))

      tcp.expectMsg(Bind(server, address))
    }

    "handle connections" in {
      val client = TestProbe()
      val connection = TestProbe()

      val server = system.actorOf(ServerActor.props(testConfig(), staticIdGenerator(), testActor, staticClientFactory(client.ref)))
      server ! Bound(testAddress)
      server.tell(Connected(testAddress, testAddress), connection.ref)

      connection.expectMsg(Register(client.ref))
    }

  }

  private val testAddress = new InetSocketAddress("127.0.0.1", 0)

  private def testConfig(address: InetSocketAddress = testAddress): ServerConfig =
    ServerConfig(address)

  private def staticIdGenerator(id: String = "123"): IdGenerator =
    new IdGenerator {
      override def generate: String = id
    }

  private def staticClientFactory(actor: ActorRef = testActor): ClientFactory =
    _ => (_, _) => actor
}
