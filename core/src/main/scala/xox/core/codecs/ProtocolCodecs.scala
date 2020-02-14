package xox.core.codecs

import scodec.Codec
import xox.core.protocol.ErrorCauseModel

object ProtocolCodecs {
  import CommonCodecs._
  import scodec.codecs._

  lazy val errorCauseModelCodec: Codec[ErrorCauseModel] =
    (uint8 :: string16).as[ErrorCauseModel]
}
