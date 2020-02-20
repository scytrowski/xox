package xox.core.codecs

import scodec.{Codec, Decoder, Encoder, Err}
import xox.core.protocol.ServerCommand

object ServerCommandCodec {
  import CommonCodecs._
  import GameCodecs._
  import ServerCommand._
  import scodec.codecs._

  lazy val codec: Codec[ServerCommand] = Codec(encoder, decoder)

  lazy val encoder: Encoder[ServerCommand] =
    (uint8 ~ commandEncoder.encodeOnly)
      .xmapc { case (_, command) => command }(command =>
        commandCode(command) -> command
      )

  lazy val decoder: Decoder[ServerCommand] =
    uint8.flatMap(commandDecoder)

  private lazy val commandEncoder =
    selectedEncoder[ServerCommand](cmd =>
      Err(s"Cannot encode server command: $cmd")
    ) {
      case RequestPlayerList => requestPlayerListCodec.upcast
      case _: Login          => loginCodec.upcast
      case _: Logout         => logoutCodec.upcast
      case RequestMatchList  => requestMatchListCodec.upcast
      case _: CreateMatch    => createMatchCodec.upcast
      case _: JoinMatch      => joinMatchCodec.upcast
      case _: MakeTurn       => makeTurnCodec.upcast
    }

  private def commandDecoder(code: Int): Decoder[ServerCommand] =
    code match {
      case 1       => requestPlayerListCodec
      case 2       => loginCodec
      case 3       => logoutCodec
      case 4       => requestMatchListCodec
      case 5       => createMatchCodec
      case 6       => joinMatchCodec
      case 7       => makeTurnCodec
      case unknown => fail(Err(s"Unknown server command code: $unknown"))
    }

  private lazy val requestPlayerListCodec = provide(RequestPlayerList)
  private lazy val loginCodec             = string16.as[Login]
  private lazy val logoutCodec            = string16.as[Logout]
  private lazy val requestMatchListCodec  = provide(RequestMatchList)
  private lazy val createMatchCodec =
    (string16 :: matchParametersCodec).as[CreateMatch]
  private lazy val joinMatchCodec = (string16 :: string16).as[JoinMatch]
  private lazy val makeTurnCodec  = (string16 :: uint8 :: uint8).as[MakeTurn]

  private def commandCode(command: ServerCommand): Int =
    command match {
      case RequestPlayerList => 1
      case _: Login          => 2
      case _: Logout         => 3
      case RequestMatchList  => 4
      case _: CreateMatch    => 5
      case _: JoinMatch      => 6
      case _: MakeTurn       => 7
    }
}
