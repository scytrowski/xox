package xox.core.game

final case class MatchInfo(
    id: String,
    ownerId: String,
    opponentId: Option[String],
    parameters: MatchParameters
)
