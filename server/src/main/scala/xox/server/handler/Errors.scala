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

  def incorrectField(x: Int, y: Int): ErrorModel =
    ErrorModel(
      IncorrectField,
      s"Field at (x=$x, y=$y) is not valid for the requested match"
    )

  def notYourTurn(playerId: String): ErrorModel =
    ErrorModel(
      NotYourTurn,
      s"Player with ID $playerId is not making his turn right now"
    )

  val matchNotStarted: ErrorModel =
    ErrorModel(
      MatchNotStarted,
      "Requested match has not been started yet"
    )

  def notInMatch(playerId: String): ErrorModel =
    ErrorModel(
      NotInMatch,
      s"Player with ID $playerId does not participate in any match"
    )

  val missingOpponent: ErrorModel =
    ErrorModel(
      MissingOpponent,
      "Opponent participating in the requested match is missing"
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
