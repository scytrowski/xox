package xox.server.handler

import xox.core.protocol.ErrorModel

object Errors {
  import xox.core.protocol.ErrorCause._

  def playerAlreadyLogged(nick: String): ErrorModel =
    ErrorModel(
      PlayerAlreadyLogged,
      s"Player with nick $nick is already logged in"
    )

  def playerAlreadyInMatch(playerId: String, matchId: String): ErrorModel =
    ErrorModel(
      PlayerAlreadyInMatch,
      s"Player with ID $playerId is already in match with ID $matchId"
    )

  def matchAlreadyStarted(matchId: String): ErrorModel =
    ErrorModel(
      MatchAlreadyStarted,
      s"Match with ID $matchId is already started"
    )

  def unknownPlayer(playerId: String): ErrorModel =
    ErrorModel(
      UnknownPlayer,
      s"Player with ID $playerId is unknown"
    )

  def unknownMatch(matchId: String): ErrorModel =
    ErrorModel(
      UnknownMatch,
      s"Match with ID $matchId is unknown"
    )
}
