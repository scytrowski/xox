package xox.server.game

import akka.actor.{Actor, ActorLogging, Props}
import xox.server.game.MatchManagerActor.{CreateMatch, CreateMatchResponse, CreateMatchResult, Get, GetResponse, JoinMatch, JoinMatchResponse, JoinMatchResult}
import xox.server.util.IdGenerator

final class MatchManagerActor private(idGenerator: IdGenerator) extends Actor with ActorLogging {
  override val receive: Receive = handleMatches(Map.empty)

  private def handleMatches(matches: Map[String, Match]): Receive = {
    case CreateMatch(ownerId) =>
      matches.values.find(_.isInvolved(ownerId)) match {
        case None    =>
          val matchId = idGenerator.generate
          val newMatch = Match.WaitingForOpponent(matchId, ownerId)
          log.debug(s"Created match $matchId owned by player $ownerId")
          sender() ! CreateMatchResponse(CreateMatchResult.Ok(matchId))
          context become handleMatches(matches + (matchId -> newMatch))
        case Some(m) =>
          log.warning(s"Requested creation of match owned by player $ownerId but he is already involved in match ${m.id}")
          sender() ! CreateMatchResponse(CreateMatchResult.AlreadyInMatch(m.id))
      }
    case JoinMatch(playerId, matchId) =>
      val involvedInOpt = matches.values.find(_.isInvolved(playerId))
      val matchOpt = matches.get(matchId)
      involvedInOpt -> matchOpt match {
        case (None, Some(m: Match.WaitingForOpponent)) =>
          val ongoingMatch = m.start(playerId)
          log.debug(s"Player $playerId has joined the match ${ongoingMatch.id}")
          sender() ! JoinMatchResponse(JoinMatchResult.Ok(ongoingMatch.ownerId))
          context become handleMatches(matches + (ongoingMatch.id -> ongoingMatch))
        case (None, Some(m: Match.Ongoing)) =>
          log.warning(s"Player $playerId requested joining to already ongoing match ${m.id}")
          sender() ! JoinMatchResponse(JoinMatchResult.AlreadyOngoing)
        case (Some(m), _) =>
          log.warning(s"Player $playerId requested joining to match $matchId but he is already involved in match ${m.id}")
          sender() ! JoinMatchResponse(JoinMatchResult.AlreadyInMatch(m.id))
        case (_, None) =>
          log.warning(s"Player $playerId requested joining to match $matchId which does not exist")
          sender() ! JoinMatchResponse(JoinMatchResult.MatchNotExist)
      }
    case Get(matchId) =>
      log.debug(s"Requested match with ID $matchId")
      sender() ! GetResponse(matches.get(matchId))
  }
}

object MatchManagerActor {
  def props(idGenerator: IdGenerator): Props = Props(new MatchManagerActor(idGenerator))

  final case class CreateMatch(ownerId: String)
  final case class CreateMatchResponse(result: CreateMatchResult)
  final case class JoinMatch(playerId: String, matchId: String)
  final case class JoinMatchResponse(result: JoinMatchResult)
  final case class Get(matchId: String)
  final case class GetResponse(requestedMatch: Option[Match])

  sealed abstract class CreateMatchResult

  object CreateMatchResult {
    final case class Ok(matchId: String) extends CreateMatchResult
    final case class AlreadyInMatch(matchId: String) extends CreateMatchResult
  }

  sealed abstract class JoinMatchResult

  object JoinMatchResult {
    final case class Ok(ownerId: String) extends JoinMatchResult
    case object AlreadyOngoing extends JoinMatchResult
    final case class AlreadyInMatch(matchId: String) extends JoinMatchResult
    case object MatchNotExist extends JoinMatchResult
  }
}
