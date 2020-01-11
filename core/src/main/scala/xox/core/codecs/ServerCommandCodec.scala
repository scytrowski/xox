package xox.core.codecs

import scodec.{Codec, Decoder, Encoder, Err}
import xox.core.protocol.ServerCommand

object ServerCommandCodec {
  import CommonCodecs._
  import ServerCommand._
  import scodec.codecs._

  lazy val codec: Codec[ServerCommand] = Codec(encoder, decoder)

  lazy val encoder: Encoder[ServerCommand] =
    (uint8 ~ commandEncoder.encodeOnly)
      .xmapc { case (_, command) => command } (command => commandCode(command) -> command)

  lazy val decoder: Decoder[ServerCommand] =
    uint8.flatMap(commandDecoder)

  private lazy val commandEncoder =
    selectedEncoder[ServerCommand](cmd => Err(s"Cannot encode server command: $cmd")) {
      case _: Login => loginCodec.upcast
    }

  private def commandDecoder(code: Int): Decoder[ServerCommand] =
    code match {
      case 1       => loginCodec
      case unknown => fail(Err(s"Unknown server command code: $unknown"))
    }

  private lazy val loginCodec = ascii.as[Login]

  private def commandCode(command: ServerCommand): Int =
    command match {
      case _: Login => 1
    }
}
