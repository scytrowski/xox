package xox.server.handler

import akka.actor.ActorRef
import akka.testkit.TestProbe
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.ActorSpec
import xox.server.game.MatchManagerActor.{CreateMatch, CreateMatchResponse, CreateMatchResult, JoinMatch, JoinMatchResponse, JoinMatchResult}
import xox.server.game.PlayerManagerActor.{Login, LoginResponse, LoginResult}
import xox.server.handler.ClientManagerActor.SendCommand
import xox.server.handler.CommandManagerActor.HandleCommand

class CommandHandlerActorTest extends ActorSpec("CommandHandlerTest") {
  "CommandHandler" when {

    "Login" should {

      "inform player has been logged in successfully" in {
        val recipient = TestProbe()
        val playerManager = TestProbe()
        val commandHandler = createHandler(playerManager = playerManager.ref)

        val request = CommandRequest("123", ServerCommand.Login("abc"), recipient.ref)
        commandHandler ! HandleCommand(request)

        playerManager.expectMsg(Login("123", "abc"))
        commandHandler ! LoginResponse(LoginResult.Ok("456"))
        recipient.expectMsg(SendCommand("123", ClientCommand.LoginOk("456")))
      }

      "inform player is already logged in" in {
        val recipient = TestProbe()
        val playerManager = TestProbe()
        val commandHandler = createHandler(playerManager = playerManager.ref)

        val request = CommandRequest("123", ServerCommand.Login("abc"), recipient.ref)
        commandHandler ! HandleCommand(request)

        playerManager.expectMsg(Login("123", "abc"))
        commandHandler ! LoginResponse(LoginResult.AlreadyLogged)
        recipient.expectMsgPF() { case SendCommand("123", _: ClientCommand.Error) => }
      }

    }

    "CreateMatch" should {

      "inform match has been created successfully" in {
        val recipient = TestProbe()
        val matchManager = TestProbe()
        val commandHandler = createHandler(matchManager = matchManager.ref)

        val request = CommandRequest("123", ServerCommand.CreateMatch("456"), recipient.ref)
        commandHandler ! HandleCommand(request)

        matchManager.expectMsg(CreateMatch("456"))
        commandHandler ! CreateMatchResponse(CreateMatchResult.Ok("789"))
        recipient.expectMsg(SendCommand("123", ClientCommand.CreateMatchOk("789")))
      }

      "inform requesting player is already in some match" in {
        val recipient = TestProbe()
        val matchManager = TestProbe()
        val commandHandler = createHandler(matchManager = matchManager.ref)

        val request = CommandRequest("123", ServerCommand.CreateMatch("456"), recipient.ref)
        commandHandler ! HandleCommand(request)

        matchManager.expectMsg(CreateMatch("456"))
        commandHandler ! CreateMatchResponse(CreateMatchResult.AlreadyInMatch("789"))
        recipient.expectMsgPF() { case SendCommand("123", _: ClientCommand.Error) => }
      }

    }

    "JoinMatch" should {

      "inform has joined the match successfully" in {
        val recipient = TestProbe()
        val matchManager = TestProbe()
        val commandHandler = createHandler(matchManager = matchManager.ref)

        val request = CommandRequest("123", ServerCommand.JoinMatch("456", "789"), recipient.ref)
        commandHandler ! HandleCommand(request)

        matchManager.expectMsg(JoinMatch("456", "789"))
        commandHandler ! JoinMatchResponse(JoinMatchResult.Ok("abc"))
        recipient.expectMsg(SendCommand("123", ClientCommand.JoinMatchOk("abc")))
      }

      "inform requested match is already ongoing" in {
        val recipient = TestProbe()
        val matchManager = TestProbe()
        val commandHandler = createHandler(matchManager = matchManager.ref)

        val request = CommandRequest("123", ServerCommand.JoinMatch("456", "789"), recipient.ref)
        commandHandler ! HandleCommand(request)

        matchManager.expectMsg(JoinMatch("456", "789"))
        commandHandler ! JoinMatchResponse(JoinMatchResult.AlreadyOngoing)
        recipient.expectMsgPF() { case SendCommand("123", _: ClientCommand.Error) => }
      }

      "inform requesting player is already in some match" in {
        val recipient = TestProbe()
        val matchManager = TestProbe()
        val commandHandler = createHandler(matchManager = matchManager.ref)

        val request = CommandRequest("123", ServerCommand.JoinMatch("456", "789"), recipient.ref)
        commandHandler ! HandleCommand(request)

        matchManager.expectMsg(JoinMatch("456", "789"))
        commandHandler ! JoinMatchResponse(JoinMatchResult.AlreadyInMatch("abc"))
        recipient.expectMsgPF() { case SendCommand("123", _: ClientCommand.Error) => }
      }

      "inform requested match does not exist" in {
        val recipient = TestProbe()
        val matchManager = TestProbe()
        val commandHandler = createHandler(matchManager = matchManager.ref)

        val request = CommandRequest("123", ServerCommand.JoinMatch("456", "789"), recipient.ref)
        commandHandler ! HandleCommand(request)

        matchManager.expectMsg(JoinMatch("456", "789"))
        commandHandler ! JoinMatchResponse(JoinMatchResult.MatchNotExist)
        recipient.expectMsgPF() { case SendCommand("123", _: ClientCommand.Error) => }
      }

    }

  }

  private def createHandler(playerManager: ActorRef = testActor, matchManager: ActorRef = testActor): ActorRef =
    system.actorOf(CommandHandlerActor.props(playerManager, matchManager))
}
