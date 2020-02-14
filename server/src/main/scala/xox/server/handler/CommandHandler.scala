package xox.server.handler

import cats.data.State
import xox.core.game.MatchParameters
import xox.core.protocol.{ClientCommand, ErrorCause, ServerCommand}
import xox.server.ServerState
import xox.server.ServerState.{CreateMatchResult, JoinMatchResult, LoginResult}
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
  import ServerCommand._
  import ErrorCause._

  override def handle(
      command: IncomingCommand
  ): State[ServerState, List[OutgoingCommand]] =
    command.command match {
      case Login(nick) => handleLogin(command.clientId, nick)
      case CreateMatch(playerId, parameters) =>
        handleCreateMatch(command.clientId, playerId, parameters)
      case JoinMatch(playerId, matchId) =>
        handleJoinMatch(command.clientId, playerId, matchId)
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
          state -> error(clientId, PlayerAlreadyLogged(nick))
      }
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
          state -> error(clientId, PlayerAlreadyInMatch(playerId, alreadyInId))
        case CreateMatchResult.UnknownPlayer =>
          state -> error(clientId, UnknownPlayer(playerId))
      }
    }

  private def handleJoinMatch(
      clientId: String,
      playerId: String,
      matchId: String
  ): State[ServerState, List[OutgoingCommand]] =
    State { state =>
      state.joinMatch(matchId, playerId) match {
        case JoinMatchResult.Ok(updatedState, _, ownerMark) =>
          val commands = List(
            Private(clientId, JoinMatchOk(matchId, playerId, ownerMark)),
            Broadcast(MatchStarted(matchId, playerId, ownerMark))
          )
          updatedState -> commands
        case JoinMatchResult.AlreadyStarted =>
          state -> error(clientId, MatchAlreadyStarted(matchId))
        case JoinMatchResult.AlreadyInMatch(alreadyInId) =>
          state -> error(clientId, PlayerAlreadyInMatch(playerId, alreadyInId))
        case JoinMatchResult.UnknownPlayer =>
          state -> error(clientId, UnknownPlayer(playerId))
        case JoinMatchResult.UnknownMatch =>
          state -> error(clientId, UnknownMatch(matchId))
      }
    }

  private def error(
      clientId: String,
      cause: ErrorCause
  ): List[OutgoingCommand] =
    Private(clientId, Error(cause)) :: Nil
}
