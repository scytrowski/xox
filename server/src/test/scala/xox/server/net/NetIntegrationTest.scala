package xox.server.net

import java.net.InetSocketAddress

import akka.io.{IO, Tcp}
import akka.testkit.{SocketUtil, TestProbe}
import org.scalatest.EitherValues
import xox.core.codecs.ServerCommandCodec
import xox.core.protocol.ServerCommand
import xox.core.protocol.ServerCommand.Login
import xox.server.ActorSpec
import xox.server.config.ServerConfig
import xox.server.fixture.TestTcpClientActor
import xox.server.fixture.TestTcpClientActor.{Connected, Send}
import xox.server.handler.ClientHandlerActor.{ReceivedCommand, Register}
import xox.server.net.ServerActor.ClientFactory
import xox.server.util.IdGenerator

class NetIntegrationTest extends ActorSpec("NetIntegrationTest") with EitherValues {
  "Net integration" should {

    "handle incoming commands" in {
      val address = new InetSocketAddress("127.0.0.1", SocketUtil.temporaryLocalPort())
      val config = testConfig(address)
      val idGenerator = staticIdGenerator("123")
      val tcpProbe = TestProbe()
      val clientHandler = TestProbe()
      val clientFactory: ClientFactory = refFactory => (id, connection) => refFactory.actorOf(ClientActor.props(id, connection, clientHandler.ref))
      system.actorOf(ServerActor.props(config, idGenerator, IO(Tcp), clientFactory))
      val tcpClient = system.actorOf(TestTcpClientActor.props(address, tcpProbe.ref))
      val command = Login("abc")

      tcpProbe.expectMsg(Connected)
      clientHandler.expectMsgType[Register]
      tcpClient ! Send(incomingCommand(command))
      clientHandler.expectMsg(ReceivedCommand("123", command))
    }

  }

  private def testConfig(address: InetSocketAddress): ServerConfig =
    ServerConfig(address)

  private def incomingCommand(command: ServerCommand): Array[Byte] =
    ServerCommandCodec.encoder
      .encode(command)
      .toEither
      .right
      .value
      .toByteArray

  private def staticIdGenerator(id: String): IdGenerator =
    new IdGenerator {
      override def generate: String = id
    }
}
