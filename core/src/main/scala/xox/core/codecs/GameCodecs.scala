package xox.core.codecs

import scodec.Codec
import xox.core.game.{Mark, MatchInfo, MatchParameters}

object GameCodecs {
  import scodec.codecs._

  val markCodec: Codec[Mark] = bool.xmapc[Mark](b => if (b) Mark.X else Mark.O)(_ == Mark.X)
  val matchParametersCodec: Codec[MatchParameters] = uint8.as[MatchParameters]
  val matchInfoCodec: Codec[MatchInfo] = (ascii :: optional(bool, ascii) :: matchParametersCodec).as[MatchInfo]
}
