package xox.server

import xox.core.game.{Mark, MatchInfo, MatchParameters, PlayerInfo}
import xox.server.ServerState.{
  CreateMatchResult,
  JoinMatchResult,
  LoginResult,
  LogoutResult
}
import xox.server.game.{Match, MatchStateFactory, Player}
import xox.server.util.IdGenerator

trait ServerState {
  def playerList: List[PlayerInfo]
  def login(nick: String, clientId: String): LoginResult
  def logout(playerId: String): LogoutResult
  def matchList: List[MatchInfo]
  def createMatch(
      ownerId: String,
      parameters: MatchParameters
  ): CreateMatchResult
  def joinMatch(matchId: String, playerId: String): JoinMatchResult
}

object ServerState {
  sealed abstract class LoginResult

  object LoginResult {
    final case class Ok(state: ServerState, playerId: String)
        extends LoginResult
    case object AlreadyLogged extends LoginResult
  }

  sealed abstract class LogoutResult

  object LogoutResult {
    final case class Ok(state: ServerState) extends LogoutResult
    case object UnknownPlayer               extends LogoutResult
  }

  sealed abstract class CreateMatchResult

  object CreateMatchResult {
    final case class Ok(state: ServerState, matchId: String)
        extends CreateMatchResult
    final case class AlreadyInMatch(matchId: String) extends CreateMatchResult
    case object UnknownPlayer                        extends CreateMatchResult
  }

  sealed abstract class JoinMatchResult

  object JoinMatchResult {
    final case class Ok(state: ServerState, ownerId: String, ownerMark: Mark)
        extends JoinMatchResult
    case object AlreadyStarted                       extends JoinMatchResult
    final case class AlreadyInMatch(matchId: String) extends JoinMatchResult
    case object UnknownPlayer                        extends JoinMatchResult
    case object UnknownMatch                         extends JoinMatchResult
  }
}

final case class ServerStateLive(
    players: Map[String, Player],
    matches: Map[String, Match],
    private val idGenerator: IdGenerator,
    private val matchStateFactory: MatchStateFactory
) extends ServerState {
  override def playerList: List[PlayerInfo] =
    players.values.map(_.toInfo).toList

  def login(nick: String, clientId: String): LoginResult =
    findPlayerByNick(nick) match {
      case None =>
        val playerId     = idGenerator.generate
        val player       = Player(playerId, nick, clientId)
        val updatedState = copy(players = players.updated(playerId, player))
        LoginResult.Ok(updatedState, playerId)
      case Some(_) =>
        LoginResult.AlreadyLogged
    }

  def logout(playerId: String): LogoutResult =
    findPlayerById(playerId) match {
      case Some(_) =>
        val updatedState = copy(players - playerId)
        LogoutResult.Ok(updatedState)
      case None =>
        LogoutResult.UnknownPlayer
    }

  override def matchList: List[MatchInfo] =
    matches.values.map(_.toInfo).toList

  def createMatch(
      ownerId: String,
      parameters: MatchParameters
  ): CreateMatchResult =
    findPlayerById(ownerId) -> findMatchByPlayerId(ownerId) match {
      case (Some(_), None) =>
        val matchId      = idGenerator.generate
        val newMatch     = Match.WaitingForOpponent(matchId, ownerId, parameters)
        val updatedState = copy(matches = matches.updated(matchId, newMatch))
        CreateMatchResult.Ok(updatedState, matchId)
      case (Some(_), Some(alreadyIn)) =>
        CreateMatchResult.AlreadyInMatch(alreadyIn.id)
      case (None, _) =>
        CreateMatchResult.UnknownPlayer
    }

  def joinMatch(matchId: String, playerId: String): JoinMatchResult =
    (
      findPlayerById(playerId),
      findMatchById(matchId),
      findMatchByPlayerId(playerId)
    ) match {
      case (Some(_), Some(notStarted: Match.WaitingForOpponent), None) =>
        val startedMatch = notStarted.start(playerId)(matchStateFactory.create)
        val updatedState =
          copy(matches = matches.updated(startedMatch.id, startedMatch))
        JoinMatchResult.Ok(
          updatedState,
          startedMatch.ownerId,
          startedMatch.state.ownerMark
        )
      case (Some(_), Some(_: Match.Ongoing), None) =>
        JoinMatchResult.AlreadyStarted
      case (Some(_), _, Some(alreadyIn)) =>
        JoinMatchResult.AlreadyInMatch(alreadyIn.id)
      case (None, _, _) =>
        JoinMatchResult.UnknownPlayer
      case (_, None, _) =>
        JoinMatchResult.UnknownMatch
    }

  private def findPlayerById(id: String): Option[Player] = players.get(id)

  private def findPlayerByNick(nick: String): Option[Player] =
    players.values.find(_.nick == nick)

  private def findMatchById(id: String): Option[Match] = matches.get(id)

  private def findMatchByPlayerId(playerId: String): Option[Match] =
    matches.values.find(_.isInvolved(playerId))
}
