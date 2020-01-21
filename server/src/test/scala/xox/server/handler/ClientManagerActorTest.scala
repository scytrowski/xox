package xox.server.handler

import akka.testkit.TestProbe
import xox.core.protocol.ClientCommand.LoginOk
import xox.core.protocol.ServerCommand.Login
import xox.server.ActorSpec
import xox.server.game.PlayerManagerActor.LogoutAll
import xox.server.handler.ClientManagerActor.{ReceivedCommand, Register, SendCommand, Unregister}
import xox.server.handler.CommandManagerActor.HandleCommand

class ClientManagerActorTest extends ActorSpec("ClientManagerActorTest") {
  "ClientManagerActor" should {

    "handle incoming command from known client" in {
      val commandManager = TestProbe()
      val command = Login("abc")

      val clientManager = system.actorOf(ClientManagerActor.props(commandManager.ref, testActor))

      clientManager ! Register("123", testActor)
      clientManager ! ReceivedCommand("123", command)
      commandManager.expectMsg(HandleCommand(CommandRequest("123", command, clientManager)))
    }

    "send outgoing command to known client" in {
      val client = TestProbe()
      val command = LoginOk("abc")

      val clientManager = system.actorOf(ClientManagerActor.props(testActor, testActor))

      clientManager ! Register("123", client.ref)
      clientManager ! SendCommand("123", command)
      client.expectMsg(command)
    }

    "logout all players handled by client on unregister" in {
      val playerManager = TestProbe()

      val clientManager = system.actorOf(ClientManagerActor.props(testActor, playerManager.ref))

      clientManager ! Register("123", testActor)
      clientManager ! Unregister("123")
      playerManager.expectMsg(LogoutAll("123"))
    }

  }
}
