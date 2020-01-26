package xox.core.game

final case class MatchInfo(ownerId: String, opponentId: Option[String], parameters: MatchParameters)
