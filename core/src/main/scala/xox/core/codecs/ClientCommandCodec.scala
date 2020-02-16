package xox.core.codecs

import scodec.{Codec, Decoder, Encoder, Err}
import xox.core.protocol.ClientCommand

object ClientCommandCodec {
  import ClientCommand._
  import scodec.codecs._
  import CommonCodecs._
  import ProtocolCodecs._
  import GameCodecs._

  lazy val codec: Codec[ClientCommand] = Codec(encoder, decoder)

  lazy val encoder: Encoder[ClientCommand] =
    (uint8 ~ commandEncoder.encodeOnly)
      .xmapc { case (_, command) => command }(command =>
        commandCode(command) -> command
      )

  lazy val decoder: Decoder[ClientCommand] =
    uint8.flatMap(commandDecoder)

  private lazy val commandEncoder =
    selectedEncoder[ClientCommand](cmd =>
      Err(s"Cannot encode client command: $cmd")
    ) {
      case _: PlayerList      => playerListCodec.upcast
      case _: LoginOk         => loginOkCodec.upcast
      case LogoutOk           => logoutOkCodec.upcast
      case _: PlayerLogged    => playerLoggedCodec.upcast
      case _: PlayerLoggedOut => playerLoggedOutCodec.upcast
      case _: MatchList       => matchListCodec.upcast
      case _: CreateMatchOk   => createMatchOkCodec.upcast
      case _: JoinMatchOk     => joinMatchOkCodec.upcast
      case _: MatchCreated    => matchCreatedCodec.upcast
      case _: MatchStarted    => matchStartedCodec.upcast
      case _: MatchFinished   => matchFinishedCodec.upcast
      case Timeout            => timeoutCodec.upcast
      case _: Error           => errorCodec.upcast
    }

  private def commandDecoder(code: Int): Decoder[ClientCommand] =
    code match {
      case 1       => playerListCodec
      case 2       => loginOkCodec
      case 3       => logoutOkCodec
      case 4       => playerLoggedCodec
      case 5       => playerLoggedOutCodec
      case 6       => matchListCodec
      case 7       => createMatchOkCodec
      case 8       => joinMatchOkCodec
      case 9       => matchCreatedCodec
      case 10      => matchStartedCodec
      case 11      => matchFinishedCodec
      case 254     => timeoutCodec
      case 255     => errorCodec
      case unknown => fail(Err(s"Unknown client command code: $unknown"))
    }

  private lazy val playerListCodec =
    listOfN(uint8, playerInfoCodec).as[PlayerList]
  private lazy val loginOkCodec         = string16.as[LoginOk]
  private lazy val logoutOkCodec        = provide(LogoutOk)
  private lazy val playerLoggedCodec    = (string16 :: string16).as[PlayerLogged]
  private lazy val playerLoggedOutCodec = string16.as[PlayerLoggedOut]
  private lazy val matchListCodec       = listOfN(uint8, matchInfoCodec).as[MatchList]
  private lazy val createMatchOkCodec   = (string16 :: string16).as[CreateMatchOk]
  private lazy val joinMatchOkCodec =
    (string16 :: string16 :: markCodec).as[JoinMatchOk]
  private lazy val matchCreatedCodec =
    (string16 :: string16 :: matchParametersCodec).as[MatchCreated]
  private lazy val matchStartedCodec =
    (string16 :: string16 :: markCodec).as[MatchStarted]
  private lazy val matchFinishedCodec =
    (string16 :: optional(bool, string16)).as[MatchFinished]
  private lazy val timeoutCodec = provide(Timeout)
  private lazy val errorCodec   = errorCauseModelCodec.as[Error]

  private def commandCode(command: ClientCommand): Int =
    command match {
      case _: PlayerList      => 1
      case _: LoginOk         => 2
      case LogoutOk           => 3
      case _: PlayerLogged    => 4
      case _: PlayerLoggedOut => 5
      case _: MatchList       => 6
      case _: CreateMatchOk   => 7
      case _: JoinMatchOk     => 8
      case _: MatchCreated    => 9
      case _: MatchStarted    => 10
      case _: MatchFinished   => 11
      case Timeout            => 254
      case _: Error           => 255
    }
}
