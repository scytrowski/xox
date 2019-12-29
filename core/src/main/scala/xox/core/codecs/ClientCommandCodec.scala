package xox.core.codecs

import scodec.{Codec, Decoder, Encoder, Err}
import xox.core.protocol.ClientCommand

object ClientCommandCodec {
  import ClientCommand._
  import scodec.codecs._
  import CommonCodecs._

  lazy val codec: Codec[ClientCommand] = Codec(encoder, decoder)

  lazy val encoder: Encoder[ClientCommand] =
    selectedEncoder[ClientCommand](cmd => Err(s"Cannot encode client command: $cmd")) {
      case _: LoginOk => loginOkCodec.upcast
    }

  lazy val decoder: Decoder[ClientCommand] =
    uint8.flatMap {
      case 1       => loginOkCodec
      case unknown => fail(Err(s"Unknown client command code: $unknown"))
    }

  private lazy val loginOkCodec = ascii.as[LoginOk]
}
