package xox.server.handler

import akka.actor.ActorRef
import akka.testkit.TestProbe
import xox.core.protocol.ClientCommand.LoginOk
import xox.core.protocol.ServerCommand.Login
import xox.server.ActorSpec
import xox.server.handler.ClientManagerActor.{CommandHandlerFactory, ReceivedCommand, Register, SendCommand}
import xox.server.handler.CommandHandlerActor.HandleCommand

class ClientManagerActorTest extends ActorSpec("ClientManagerActorTest") {
  "ClientManagerActor" should {

    "handle incoming command from known client" in {
      val commandHandler = TestProbe()
      val command = Login("abc")

      val clientManager = system.actorOf(ClientManagerActor.props(staticCommandHandlerFactory(commandHandler.ref)))

      clientManager ! Register("123", testActor)
      clientManager ! ReceivedCommand("123", command)
      commandHandler.expectMsg(HandleCommand("123", command))
    }

    "send outgoing command to known client" in {
      val client = TestProbe()
      val command = LoginOk("abc")

      val clientManager = system.actorOf(ClientManagerActor.props(staticCommandHandlerFactory()))

      clientManager ! Register("123", client.ref)
      clientManager ! SendCommand("123", command)
      client.expectMsg(command)
    }

  }

  private def staticCommandHandlerFactory(handler: ActorRef = testActor): CommandHandlerFactory =
    _ => _ => handler
}
