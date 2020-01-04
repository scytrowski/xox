package xox.server.net

import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.testkit.TestProbe
import akka.util.ByteString
import org.scalatest.EitherValues
import scodec.Encoder
import xox.core.codecs.{ClientCommandCodec, ServerCommandCodec}
import xox.core.protocol.ClientCommand.LoginOk
import xox.core.protocol.ServerCommand.Login
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.ActorSpec
import xox.server.handler.ClientHandlerActor.{ReceivedCommand, Register, Unregister}

class ClientActorTest extends ActorSpec("ClientActorTest") with EitherValues {
  "ClientActor" should {

    "register when created" in {
      val clientId = "123"
      val clientHandler = TestProbe()

      val client = system.actorOf(ClientActor.props(clientId, testActor, clientHandler.ref))

      clientHandler.expectMsg(Register(clientId, client))
    }

    "receive incoming commands" in {
      val clientHandler = TestProbe()
      val command = Login("abc")

      val client = system.actorOf(ClientActor.props("123", testActor, clientHandler.ref))

      clientHandler.expectMsgType[Register]
      client ! Received(incomingCommands(command))
      clientHandler.expectMsg(ReceivedCommand("123", command))
    }

    "send outgoing commands" in {
      val connection = TestProbe()
      val command = LoginOk("abc")

      val client = system.actorOf(ClientActor.props("123", connection.ref, testActor))
      client ! command

      connection.expectMsg(Write(outgoingCommand(command)))
    }

    "unregister when connection closed" in {
      val clientId = "123"
      val clientHandler = TestProbe()

      val client = system.actorOf(ClientActor.props(clientId, testActor, clientHandler.ref))

      clientHandler.expectMsgType[Register]
      client ! PeerClosed
      clientHandler.expectMsg(Unregister(clientId))
    }

  }

  private def incomingCommands(commands: ServerCommand*): ByteString =
    ByteString {
      serverCommandEncoder.encode(commands.toList)
        .toEither
        .right
        .value
        .toByteArray
    }

  private def outgoingCommand(command: ClientCommand): ByteString =
    ByteString {
      ClientCommandCodec.encoder
        .encode(command)
        .toEither
        .right
        .value
        .toByteArray
    }

  private val serverCommandEncoder: Encoder[List[ServerCommand]] =
    scodec.codecs.list(ServerCommandCodec.encoder.encodeOnly)
}
