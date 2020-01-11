package xox.core.codecs

import scodec.{Codec, Decoder, Encoder, Err}
import xox.core.protocol.ClientCommand

object ClientCommandCodec {
  import ClientCommand._
  import scodec.codecs._
  import CommonCodecs._

  lazy val codec: Codec[ClientCommand] = Codec(encoder, decoder)

  lazy val encoder: Encoder[ClientCommand] =
    (uint8 ~ commandEncoder.encodeOnly)
      .xmapc { case (_, command) => command } (command => commandCode(command) -> command)

  lazy val decoder: Decoder[ClientCommand] =
    uint8.flatMap(commandDecoder)

  private lazy val commandEncoder =
    selectedEncoder[ClientCommand](cmd => Err(s"Cannot encode client command: $cmd")) {
      case _: LoginOk => loginOkCodec.upcast
      case Timeout    => timeoutCodec.upcast
      case _: Error   => errorCodec.upcast
    }

  private def commandDecoder(code: Int): Decoder[ClientCommand] =
    code match {
      case 1       => loginOkCodec
      case 254     => timeoutCodec
      case 255     => errorCodec
      case unknown => fail(Err(s"Unknown client command code: $unknown"))
    }

  private lazy val loginOkCodec = ascii.as[LoginOk]
  private lazy val timeoutCodec = provide(Timeout)
  private lazy val errorCodec = ascii.as[Error]

  private def commandCode(command: ClientCommand): Int =
    command match {
      case _: LoginOk => 1
      case Timeout    => 254
      case _: Error   => 255
    }
}
