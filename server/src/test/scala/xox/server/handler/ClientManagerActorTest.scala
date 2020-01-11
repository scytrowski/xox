package xox.server.handler

import akka.testkit.TestProbe
import xox.core.protocol.ClientCommand.LoginOk
import xox.core.protocol.ServerCommand.Login
import xox.server.ActorSpec
import xox.server.handler.ClientManagerActor.{ReceivedCommand, Register, SendCommand}
import xox.server.handler.CommandManagerActor.HandleCommand

class ClientManagerActorTest extends ActorSpec("ClientManagerActorTest") {
  "ClientManagerActor" should {

    "handle incoming command from known client" in {
      val commandManager = TestProbe()
      val command = Login("abc")

      val clientManager = system.actorOf(ClientManagerActor.props(commandManager.ref))

      clientManager ! Register("123", testActor)
      clientManager ! ReceivedCommand("123", command)
      commandManager.expectMsg(HandleCommand(CommandRequest("123", command, clientManager)))
    }

    "send outgoing command to known client" in {
      val client = TestProbe()
      val command = LoginOk("abc")

      val clientManager = system.actorOf(ClientManagerActor.props(testActor))

      clientManager ! Register("123", client.ref)
      clientManager ! SendCommand("123", command)
      client.expectMsg(command)
    }

  }
}
