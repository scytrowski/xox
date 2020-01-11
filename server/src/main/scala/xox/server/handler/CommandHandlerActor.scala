package xox.server.handler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.game.PlayerManagerActor.{Login, LoginResponse, LoginResult}
import xox.server.handler.ClientManagerActor.SendCommand
import xox.server.handler.CommandManagerActor.HandleCommand

final class CommandHandlerActor(playerManager: ActorRef) extends Actor with ActorLogging {
  override val receive: Receive = {
    case HandleCommand(request) =>
      log.debug(s"Handling ${request.command} command sent by client ${request.clientId}")
      request.command match {
        case ServerCommand.Login(nick) =>
          playerManager ! Login(request.clientId, nick)
          context become handleLogin(request, nick)
      }
  }

  private def handleLogin(request: CommandRequest, nick: String): Receive = {
    case LoginResponse(result) =>
      result match {
        case LoginResult.Ok(playerId)  =>
          log.debug(s"Player $nick handled by client ${request.clientId} has been successfully logged in and associated with ID $playerId")
          request.recipient ! SendCommand(request.clientId, ClientCommand.LoginOk(playerId))
        case LoginResult.AlreadyLogged =>
          log.debug(s"Player $nick is already logged in")
          request.recipient ! SendCommand(request.clientId, ClientCommand.Error(s"Player with nick $nick is already logged in"))
      }
      context stop self
  }
}

object CommandHandlerActor {
  def props(playerManager: ActorRef): Props = Props(new CommandHandlerActor(playerManager))
}
