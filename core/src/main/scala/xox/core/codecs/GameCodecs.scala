package xox.core.codecs

import scodec.{Attempt, Codec, Err}
import xox.core.game.{Mark, MatchInfo, MatchParameters}

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
  val matchInfoCodec: Codec[MatchInfo] =
    (string16 :: optional(bool, string16) :: matchParametersCodec).as[MatchInfo]
}