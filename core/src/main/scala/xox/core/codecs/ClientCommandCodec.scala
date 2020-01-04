package xox.core.codecs

import scodec.{Codec, Decoder, Encoder, Err}
import xox.core.protocol.ClientCommand

object ClientCommandCodec {
  import ClientCommand._
  import scodec.codecs._
  import CommonCodecs._

  lazy val codec: Codec[ClientCommand] = Codec(encoder, decoder)

  lazy val encoder: Encoder[ClientCommand] =
    (byte ~ commandEncoder.encodeOnly)
      .xmapc { case (_, command) => command } (command => commandCode(command) -> command)

  lazy val decoder: Decoder[ClientCommand] =
    byte.flatMap(commandDecoder)

  private lazy val commandEncoder =
    selectedEncoder[ClientCommand](cmd => Err(s"Cannot encode client command: $cmd")) {
      case _: LoginOk =>  loginOkCodec.upcast
    }

  private def commandDecoder(code: Byte): Decoder[ClientCommand] =
    code match {
      case 1       => loginOkCodec
      case unknown => fail(Err(s"Unknown client command code: $unknown"))
    }

  private lazy val loginOkCodec = ascii.as[LoginOk]

  private def commandCode(command: ClientCommand): Byte =
    command match {
      case _: LoginOk => 1
    }
}
