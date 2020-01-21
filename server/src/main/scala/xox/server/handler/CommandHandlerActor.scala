package xox.server.handler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.game.MatchManagerActor.{CreateMatch, CreateMatchResponse, CreateMatchResult, JoinMatch, JoinMatchResponse, JoinMatchResult}
import xox.server.game.PlayerManagerActor.{Login, LoginResponse, LoginResult}
import xox.server.handler.ClientManagerActor.SendCommand
import xox.server.handler.CommandManagerActor.HandleCommand

final class CommandHandlerActor(playerManager: ActorRef,
                                matchManager: ActorRef) extends Actor with ActorLogging {
  override val receive: Receive = {
    case HandleCommand(request) =>
      log.debug(s"Handling ${request.command} command sent by client ${request.clientId}")
      request.command match {
        case ServerCommand.Login(nick) =>
          playerManager ! Login(request.clientId, nick)
          context become handleLogin(request, nick)
        case ServerCommand.CreateMatch(playerId) =>
          matchManager ! CreateMatch(playerId)
          context become handleCreateMatch(request, playerId)
        case ServerCommand.JoinMatch(playerId, matchId) =>
          matchManager ! JoinMatch(playerId, matchId)
          context become handleJoinMatch(request, playerId, matchId)
      }
  }

  private def handleLogin(request: CommandRequest, nick: String): Receive = {
    case LoginResponse(result) =>
      result match {
        case LoginResult.Ok(playerId)  =>
          log.debug(s"Player $nick handled by client ${request.clientId} has been successfully logged in and associated with ID $playerId")
          sendCommand(request, ClientCommand.LoginOk(playerId))
        case LoginResult.AlreadyLogged =>
          log.debug(s"Player $nick is already logged in")
          sendError(request, s"Player with nick $nick is already logged in")
      }
      context stop self
  }

  private def handleCreateMatch(request: CommandRequest, playerId: String): Receive = {
    case CreateMatchResponse(result) =>
      result match {
        case CreateMatchResult.Ok(matchId) =>
          log.debug(s"Player with ID $playerId has successfully created a match with ID $matchId")
          sendCommand(request, ClientCommand.CreateMatchOk(matchId))
        case CreateMatchResult.AlreadyInMatch(matchId) =>
          log.debug(s"Player with ID $playerId is already in the match with ID $matchId")
          sendError(request, s"Player with ID $playerId is already in the match with ID $matchId")
      }
      context stop self
  }

  private def handleJoinMatch(request: CommandRequest, playerId: String, matchId: String): Receive = {
    case JoinMatchResponse(result) =>
      result match {
        case JoinMatchResult.Ok(ownerId) =>
          log.debug(s"Player with ID $playerId has successfully joined the match with ID $matchId")
          sendCommand(request, ClientCommand.JoinMatchOk(ownerId))
        case JoinMatchResult.AlreadyOngoing =>
          log.debug(s"Match with ID $matchId is already ongoing")
          sendError(request, s"Match with ID $matchId is already ongoing")
        case JoinMatchResult.AlreadyInMatch(currentMatchId) =>
          log.debug(s"Player with ID $playerId is already in the match with ID $currentMatchId")
          sendError(request, s"Player with ID $playerId is already in the match with ID $currentMatchId")
        case JoinMatchResult.MatchNotExist =>
          log.debug(s"Match with ID $matchId does not exist")
          sendError(request, s"Match with ID $matchId does not exist")
      }
      context stop self
  }

  private def sendError(request: CommandRequest, message: String): Unit =
    sendCommand(request, ClientCommand.Error(message))

  private def sendCommand(request: CommandRequest, command: ClientCommand): Unit =
    request.recipient ! SendCommand(request.clientId, command)
}

object CommandHandlerActor {
  def props(playerManager: ActorRef, matchManager: ActorRef): Props = Props(new CommandHandlerActor(playerManager, matchManager))
}
