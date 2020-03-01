package xox.core.codec

import scodec.{Attempt, Codec, Err}
import xox.core.game.{Mark, MatchInfo, MatchParameters, PlayerInfo}

object GameCodecs {
  import CommonCodecs._
  import scodec.codecs._

  val markCodec: Codec[Mark] = Codec(
    uint8.contramap[Mark] {
      case Mark.X => 0
      case Mark.O => 1
    },
    uint8.emap {
      case 0 => Attempt.successful(Mark.X)
      case 1 => Attempt.successful(Mark.O)
      case invalid =>
        Attempt.failure(Err(s"Cannot decode mark from value: $invalid"))
    }
  )
  val matchParametersCodec: Codec[MatchParameters] = uint8.as[MatchParameters]
  val playerInfoCodec: Codec[PlayerInfo]           = (string16 :: string16).as[PlayerInfo]
  val matchInfoCodec: Codec[MatchInfo] =
    (string16 :: string16 :: optional(bool, string16) :: matchParametersCodec)
      .as[MatchInfo]
}
