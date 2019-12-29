package xox.core.codecs

import scodec.{Codec, Decoder, Encoder, Err}
import xox.core.protocol.ServerCommand

object ServerCommandCodec {
  import ServerCommand._
  import scodec.codecs._
  import CommonCodecs._

  lazy val codec: Codec[ServerCommand] = Codec(encoder, decoder)

  lazy val encoder: Encoder[ServerCommand] =
    selectedEncoder[ServerCommand](cmd => Err(s"Cannot encode server command: $cmd")) {
      case _: Login => loginCodec.upcast
    }

  lazy val decoder: Decoder[ServerCommand] =
    uint8.flatMap {
      case 1       => loginCodec
      case unknown => fail(Err(s"Unknown server command code: $unknown"))
    }

  private lazy val loginCodec = (ascii :: ascii).as[Login]
}
