package xox.server.handler

import cats.data.State
import xox.core.game.MatchParameters
import xox.core.protocol.{ClientCommand, ErrorModel, ServerCommand}
import xox.server.ServerState
import xox.server.ServerState._
import xox.server.net.OutgoingCommand.{Broadcast, Private}
import xox.server.net.{IncomingCommand, OutgoingCommand}
import xox.server.util.IdGenerator

trait CommandHandler {
  def handle(
      command: IncomingCommand
  ): State[ServerState, List[OutgoingCommand]]
}

final class CommandHandlerLive(idGenerator: IdGenerator)
    extends CommandHandler {
  import ClientCommand._
  import Errors._
  import ServerCommand._

  override def handle(
      command: IncomingCommand
  ): State[ServerState, List[OutgoingCommand]] =
    command.command match {
      case RequestPlayerList => handleRequestPlayerList(command.clientId)
      case Login(nick)       => handleLogin(command.clientId, nick)
      case Logout(playerId)  => handleLogout(command.clientId, playerId)
      case RequestMatchList  => handleRequestMatchList(command.clientId)
      case CreateMatch(playerId, parameters) =>
        handleCreateMatch(command.clientId, playerId, parameters)
      case JoinMatch(playerId, matchId) =>
        handleJoinMatch(command.clientId, playerId, matchId)
      case MakeTurn(playerId, x, y) =>
        handleMakeTurn(command.clientId, playerId, x, y)

    }

  private def handleRequestPlayerList(
      clientId: String
  ): State[ServerState, List[OutgoingCommand]] =
    State { state =>
      val players  = state.playerList
      val commands = List(Private(clientId, PlayerList(players)))
      state -> commands
    }

  private def handleLogin(
      clientId: String,
      nick: String
  ): State[ServerState, List[OutgoingCommand]] =
    State { state =>
      state.login(nick, clientId) match {
        case LoginResult.Ok(updatedState, playerId) =>
          val commands = List(
            Private(clientId, LoginOk(playerId)),
            Broadcast(PlayerLogged(playerId, nick))
          )
          updatedState -> commands
        case LoginResult.AlreadyLogged =>
          state -> error(clientId, playerAlreadyLogged(nick))
      }
    }

  private def handleLogout(
      clientId: String,
      playerId: String
  ): State[ServerState, List[OutgoingCommand]] =
    State { state =>
      state.logout(playerId) match {
        case LogoutResult.Ok(updatedState) =>
          val commands = List(
            Private(clientId, LogoutOk),
            Broadcast(PlayerLoggedOut(playerId))
          )
          updatedState -> commands
        case LogoutResult.UnknownPlayer =>
          state -> error(clientId, unknownPlayer(playerId))
      }
    }

  private def handleRequestMatchList(
      clientId: String
  ): State[ServerState, List[OutgoingCommand]] =
    State { state =>
      val matches  = state.matchList
      val commands = List(Private(clientId, MatchList(matches)))
      state -> commands
    }

  private def handleCreateMatch(
      clientId: String,
      playerId: String,
      parameters: MatchParameters
  ): State[ServerState, List[OutgoingCommand]] =
    State { state =>
      state.createMatch(playerId, parameters) match {
        case CreateMatchResult.Ok(updatedState, matchId) =>
          val commands = List(
            Private(clientId, CreateMatchOk(matchId, playerId)),
            Broadcast(MatchCreated(matchId, playerId, parameters))
          )
          updatedState -> commands
        case CreateMatchResult.AlreadyInMatch(alreadyInId) =>
          state -> error(clientId, playerAlreadyInMatch(playerId, alreadyInId))
        case CreateMatchResult.UnknownPlayer =>
          state -> error(clientId, unknownPlayer(playerId))
      }
    }

  private def handleJoinMatch(
      clientId: String,
      playerId: String,
      matchId: String
  ): State[ServerState, List[OutgoingCommand]] =
    State { state =>
      state.joinMatch(matchId, playerId) match {
        case JoinMatchResult.Ok(updatedState, _, ownerMark, turnMark) =>
          val commands = List(
            Private(
              clientId,
              JoinMatchOk(matchId, playerId, ownerMark, turnMark)
            ),
            Broadcast(MatchStarted(matchId, playerId, ownerMark, turnMark))
          )
          updatedState -> commands
        case JoinMatchResult.AlreadyStarted =>
          state -> error(clientId, matchAlreadyStarted(matchId))
        case JoinMatchResult.AlreadyInMatch(alreadyInId) =>
          state -> error(clientId, playerAlreadyInMatch(playerId, alreadyInId))
        case JoinMatchResult.UnknownPlayer =>
          state -> error(clientId, unknownPlayer(playerId))
        case JoinMatchResult.UnknownMatch =>
          state -> error(clientId, unknownMatch(matchId))
      }
    }

  private def handleMakeTurn(
      clientId: String,
      playerId: String,
      x: Int,
      y: Int
  ): State[ServerState, List[OutgoingCommand]] =
    State { state =>
      state.makeTurn(playerId, x, y) match {
        case MakeTurnResult.Ok(
            updatedState,
            fieldsLeft,
            matchId,
            opponentClientId
            ) =>
          val commands = List(
            Private(clientId, MakeTurnOk(fieldsLeft)),
            Private(opponentClientId, TurnMade(matchId, x, y))
          )
          updatedState -> commands
        case MakeTurnResult.Victory(updatedState, matchId, opponentClientId) =>
          val commands = List(
            Private(clientId, MakeTurnOk(0)),
            Private(clientId, MatchWon(matchId, playerId)),
            Private(opponentClientId, MatchLost(matchId)),
            Broadcast(MatchFinished(matchId, Some(playerId)))
          )
          updatedState -> commands
        case MakeTurnResult.Draw(updatedState, matchId, opponentClientId) =>
          val commands = List(
            Private(clientId, MakeTurnOk(0)),
            Private(clientId, MatchDrawn(matchId)),
            Private(opponentClientId, MatchDrawn(matchId)),
            Broadcast(MatchFinished(matchId, None))
          )
          updatedState -> commands
        case MakeTurnResult.IncorrectField =>
          state -> error(clientId, Errors.incorrectField(x, y))
        case MakeTurnResult.NotYourTurn =>
          state -> error(clientId, Errors.notYourTurn(playerId))
        case MakeTurnResult.MatchNotStarted =>
          state -> error(clientId, Errors.matchNotStarted)
        case MakeTurnResult.NotInMatch =>
          state -> error(clientId, Errors.notInMatch(playerId))
        case MakeTurnResult.UnknownPlayer =>
          state -> error(clientId, Errors.unknownPlayer(playerId))
        case MakeTurnResult.MissingOpponent =>
          state -> error(clientId, Errors.missingOpponent)
      }
    }

  private def error(
      clientId: String,
      model: ErrorModel
  ): List[OutgoingCommand] =
    Private(clientId, Error(model)) :: Nil
}
