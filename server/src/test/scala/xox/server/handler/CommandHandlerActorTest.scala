package xox.server.handler

import akka.testkit.TestProbe
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.ActorSpec
import xox.server.game.PlayerManagerActor.{Login, LoginResponse, LoginResult}
import xox.server.handler.ClientManagerActor.SendCommand
import xox.server.handler.CommandManagerActor.HandleCommand

class CommandHandlerActorTest extends ActorSpec("CommandHandlerTest") {
  "CommandHandler" when {

    "Login" should {

      "inform player has been logged in successfully" in {
        val recipient = TestProbe()
        val playerManager = TestProbe()
        val commandHandler = system.actorOf(CommandHandlerActor.props(playerManager.ref))

        val request = CommandRequest("123", ServerCommand.Login("abc"), recipient.ref)
        commandHandler ! HandleCommand(request)

        playerManager.expectMsg(Login("123", "abc"))
        commandHandler ! LoginResponse(LoginResult.Ok("456"))
        recipient.expectMsg(SendCommand("123", ClientCommand.LoginOk("456")))
      }

      "inform player is already logged in" in {
        val recipient = TestProbe()
        val playerManager = TestProbe()
        val commandHandler = system.actorOf(CommandHandlerActor.props(playerManager.ref))

        val request = CommandRequest("123", ServerCommand.Login("abc"), recipient.ref)
        commandHandler ! HandleCommand(request)

        playerManager.expectMsg(Login("123", "abc"))
        commandHandler ! LoginResponse(LoginResult.AlreadyLogged)
        recipient.expectMsgPF() { case SendCommand("123", _: ClientCommand.Error) => }
      }

    }

  }
}
