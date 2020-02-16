package xox.core.codecs

import xox.core.protocol.{ErrorCause, ErrorModel}
import scodec.Codec

object ProtocolCodecs {
  import CommonCodecs._
  import scodec.codecs._

  lazy val errorModelCodec: Codec[ErrorModel] =
    (errorCauseCodec :: string16).as[ErrorModel]
  lazy val errorCauseCodec: Codec[ErrorCause] = valueEnumCodec(ErrorCause, byte)
}
