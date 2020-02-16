package xox.server.handler

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Inside, LoneElement, OptionValues}
import xox.core.game.{Mark, MatchInfo, MatchParameters, PlayerInfo}
import xox.core.protocol.ClientCommand.MatchCreated
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.ServerState.{
  CreateMatchResult,
  JoinMatchResult,
  LoginResult,
  LogoutResult
}
import xox.server.game.Player
import xox.server.handler.Errors._
import xox.server.mock.{TestIdGenerator, TestServerState}
import xox.server.net.IncomingCommand
import xox.server.net.OutgoingCommand.{Broadcast, Private}

import scala.util.Random

class CommandHandlerLiveTest
    extends AnyWordSpec
    with Matchers
    with OptionValues
    with LoneElement
    with Inside {

  "CommandHandlerLive" when {

    "RequestPlayerList" should {

      "send list of logged players' info" in {
        val clientId = "123"
        val info = List(
          PlayerInfo("456", "abc"),
          PlayerInfo("789", "def")
        )
        val handler = createHandler()
        val inCommand =
          IncomingCommand(clientId, ServerCommand.RequestPlayerList)
        val inputState = new TestServerState(playerListResult = info)

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.PlayerList(info)
        )
      }

    }

    "Login" should {

      "succeed" in {
        val clientId  = "123"
        val playerId  = "456"
        val handler   = createHandler(id = playerId)
        val inCommand = IncomingCommand(clientId, ServerCommand.Login("abc"))
        val inputState = new TestServerState(
          loginResult = LoginResult.Ok(new TestServerState(), playerId)
        )

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands must contain theSameElementsInOrderAs List(
          Private(clientId, ClientCommand.LoginOk(playerId)),
          Broadcast(ClientCommand.PlayerLogged(playerId, "abc"))
        )
      }

      "inform player with requested nick is already logged in" in {
        val player  = Player("456", "abc", "123")
        val handler = createHandler()
        val inCommand =
          IncomingCommand(player.clientId, ServerCommand.Login(player.nick))
        val inputState =
          new TestServerState(loginResult = LoginResult.AlreadyLogged)

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          "123",
          ClientCommand.Error(playerAlreadyLogged(player.nick))
        )
      }

    }

    "Logout" should {

      "succeed" in {
        val clientId = "123"
        val playerId = "456"
        val handler  = createHandler()
        val inCommand =
          IncomingCommand(clientId, ServerCommand.Logout(playerId))
        val inputState = new TestServerState(
          logoutResult = LogoutResult.Ok(new TestServerState())
        )

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands must contain theSameElementsInOrderAs List(
          Private(clientId, ClientCommand.LogoutOk),
          Broadcast(ClientCommand.PlayerLoggedOut(playerId))
        )
      }

      "inform requested player is unknown" in {
        val clientId = "123"
        val playerId = "456"
        val handler  = createHandler()
        val inCommand =
          IncomingCommand(clientId, ServerCommand.Logout(playerId))
        val inputState =
          new TestServerState(logoutResult = LogoutResult.UnknownPlayer)

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.Error(unknownPlayer(playerId))
        )
      }

    }

    "RequestMatchList" should {

      "send list of ongoing matches' info" in {
        val clientId = "123"
        val info = List(
          MatchInfo("abc", "456", None, MatchParameters(3)),
          MatchInfo("def", "789", Some("012"), MatchParameters(4))
        )
        val handler = createHandler()
        val inCommand =
          IncomingCommand(clientId, ServerCommand.RequestMatchList)
        val inputState = new TestServerState(matchListResult = info)

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.MatchList(info)
        )
      }

    }

    "CreateMatch" should {

      "succeed" in {
        val clientId        = "123"
        val playerId        = "456"
        val matchId         = "789"
        val matchParameters = MatchParameters(4)
        val handler         = createHandler(matchId)
        val inCommand = IncomingCommand(
          clientId,
          ServerCommand.CreateMatch(playerId, matchParameters)
        )
        val inputState = new TestServerState(
          createMatchResult =
            CreateMatchResult.Ok(new TestServerState(), matchId)
        )

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands must contain theSameElementsInOrderAs List(
          Private(clientId, ClientCommand.CreateMatchOk(matchId, playerId)),
          Broadcast(MatchCreated(matchId, playerId, matchParameters))
        )
      }

      "inform requesting player is already in some match" in {
        val clientId    = "123"
        val playerId    = "456"
        val alreadyInId = "789"
        val handler     = createHandler()
        val inCommand = IncomingCommand(
          clientId,
          ServerCommand.CreateMatch(playerId, MatchParameters(4))
        )
        val inputState = new TestServerState(
          createMatchResult = CreateMatchResult.AlreadyInMatch(alreadyInId)
        )

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.Error(playerAlreadyInMatch(playerId, alreadyInId))
        )
      }

      "inform requesting player is unknown" in {
        val clientId = "123"
        val handler  = createHandler()
        val inCommand = IncomingCommand(
          clientId,
          ServerCommand.CreateMatch("456", MatchParameters(4))
        )
        val inputState = new TestServerState(
          createMatchResult = CreateMatchResult.UnknownPlayer
        )

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.Error(unknownPlayer("456"))
        )
      }

    }

    "JoinMatch" should {

      "succeed" in {
        val clientId   = "123"
        val matchId    = "456"
        val ownerId    = "789"
        val opponentId = "012"
        val handler    = createHandler()
        val inCommand = IncomingCommand(
          clientId,
          ServerCommand.JoinMatch(opponentId, matchId)
        )
        val inputState = new TestServerState(
          joinMatchResult =
            JoinMatchResult.Ok(new TestServerState(), ownerId, Mark.X)
        )

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands must contain theSameElementsInOrderAs List(
          Private(
            clientId,
            ClientCommand.JoinMatchOk(matchId, opponentId, Mark.X)
          ),
          Broadcast(ClientCommand.MatchStarted(matchId, opponentId, Mark.X))
        )
      }

      "inform requested match is already started" in {
        val clientId   = "123"
        val matchId    = "456"
        val opponentId = "012"
        val handler    = createHandler()
        val inCommand = IncomingCommand(
          clientId,
          ServerCommand.JoinMatch(opponentId, matchId)
        )
        val inputState =
          new TestServerState(joinMatchResult = JoinMatchResult.AlreadyStarted)

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.Error(matchAlreadyStarted(matchId))
        )
      }

      "inform requesting player is already in some match" in {
        val clientId   = "123"
        val matchId    = "456"
        val opponentId = "012"
        val handler    = createHandler()
        val inCommand = IncomingCommand(
          clientId,
          ServerCommand.JoinMatch(opponentId, matchId)
        )
        val inputState = new TestServerState(
          joinMatchResult = JoinMatchResult.AlreadyInMatch("345")
        )

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.Error(
            playerAlreadyInMatch(opponentId, "345")
          )
        )
      }

      "inform requesting player is unknown" in {
        val clientId   = "123"
        val matchId    = "456"
        val opponentId = "012"
        val handler    = createHandler()
        val inCommand = IncomingCommand(
          clientId,
          ServerCommand.JoinMatch(opponentId, matchId)
        )
        val inputState =
          new TestServerState(joinMatchResult = JoinMatchResult.UnknownPlayer)

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.Error(unknownPlayer(opponentId))
        )
      }

      "inform requested match is unknown" in {
        val clientId   = "123"
        val matchId    = "456"
        val opponentId = "012"
        val handler    = createHandler()
        val inCommand = IncomingCommand(
          clientId,
          ServerCommand.JoinMatch(opponentId, matchId)
        )
        val inputState =
          new TestServerState(joinMatchResult = JoinMatchResult.UnknownMatch)

        val (_, outCommands) = handler.handle(inCommand).run(inputState).value

        outCommands.loneElement mustBe Private(
          clientId,
          ClientCommand.Error(unknownMatch(matchId))
        )
      }

    }

  }

  private def createHandler(
      id: String = Random.nextString(10)
  ): CommandHandler =
    new CommandHandlerLive(new TestIdGenerator(id))
}
