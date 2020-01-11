package xox.server.game

import akka.actor.{Actor, ActorLogging, Props}
import xox.server.game.PlayerManagerActor.{Get, GetResponse, Login, LoginResponse, LoginResult, Logout, LogoutAll, LogoutResponse, LogoutResult}
import xox.server.util.IdGenerator

final class PlayerManagerActor private(idGenerator: IdGenerator) extends Actor with ActorLogging {
  override def receive: Receive = handlePlayers(Map.empty)

  private def handlePlayers(players: Map[String, Player]): Receive = {
    case Login(clientId, nick) =>
      players.values.find(_.nick == nick) match {
        case None                =>
          val playerId = idGenerator.generate
          val player = Player(playerId, nick, clientId)
          log.debug(s"Player $nick with ID $playerId handled by client $clientId has been logged in")
          sender() ! LoginResponse(LoginResult.Ok(playerId))
          context become handlePlayers(players + (playerId -> player))
        case Some(alreadyLogged) =>
          log.warning(s"Requested log in of player $nick but there is already one handled by client ${alreadyLogged.clientId}")
          sender() ! LoginResponse(LoginResult.AlreadyLogged)
      }
    case Logout(playerId) =>
      players.get(playerId) match {
        case Some(player) =>
          log.debug(s"Player ${player.nick} with ID $playerId handled by client ${player.clientId} has been logged out")
          sender() ! LogoutResponse(LogoutResult.Ok)
          context become handlePlayers(players - playerId)
        case None         =>
          log.warning(s"Requested log out of player with ID $playerId but there is no such player")
          sender() ! LogoutResponse(LogoutResult.NotLogged)
      }
    case LogoutAll(clientId) =>
      log.debug(s"Requested log out of all players handled by client $clientId")
      context become handlePlayers(players.filterNot { case (_, player) => player.clientId == clientId })
    case Get(playerId) =>
      log.debug(s"Requested player with ID $playerId")
      val player = players.get(playerId)
      sender() ! GetResponse(player)
  }
}

object PlayerManagerActor {
  def props(idGenerator: IdGenerator): Props = Props(new PlayerManagerActor(idGenerator))

  final case class Login(clientId: String, nick: String)
  final case class LoginResponse(result: LoginResult)
  final case class Logout(playerId: String)
  final case class LogoutResponse(result: LogoutResult)
  final case class LogoutAll(clientId: String)
  final case class Get(playerId: String)
  final case class GetResponse(player: Option[Player])

  sealed abstract class LoginResult

  object LoginResult {
    final case class Ok(playerId: String) extends LoginResult
    case object AlreadyLogged extends LoginResult
  }

  sealed abstract class LogoutResult

  object LogoutResult {
    case object Ok extends LogoutResult
    case object NotLogged extends LogoutResult
  }
}
