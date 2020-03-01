package xox.core.stream

import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import scodec.Encoder
import scodec.bits.BitVector
import xox.core.codec.ClientCommandCodec
import xox.core.fixture.StreamSpec
import xox.core.game.Mark
import xox.core.protocol.{ClientCommand, ErrorCause, ErrorModel}
import xox.core.protocol.ClientCommand.{
  Error,
  JoinMatchOk,
  LoginOk,
  MatchStarted,
  PlayerLogged
}

class EncoderFlowTest
    extends StreamSpec("EncoderFlowTest")
    with EitherValues
    with ScalaFutures {
  import scodec.codecs._

  "EncoderFlow" should {

    "encode client commands" in {
      val command1 = LoginOk("123")
      val command2 = PlayerLogged("123", "abc")
      val command3 = JoinMatchOk("456", "789", Mark.O, Mark.X)
      val command4 = MatchStarted("456", "789", Mark.O, Mark.X)
      val command5 =
        Error(ErrorModel(ErrorCause.UnknownMatch, "Something went wrong"))

      val commands = List(command1, command2, command3, command4, command5)
      val packets  = commands.map(encode)

      Source(commands)
        .via(
          EncoderFlow(ClientCommandCodec.encoder, FramingProtocol.simple(1024))
        )
        .runWith(Sink.seq)
        .futureValue must contain theSameElementsInOrderAs packets
    }

  }

  def encode(command: ClientCommand): ByteString =
    encoder
      .encode(command)
      .toEither
      .map(akkaBytes)
      .right
      .value

  private def akkaBytes(scodecBytes: BitVector): ByteString =
    ByteString(scodecBytes.toByteArray)

  private val encoder: Encoder[ClientCommand] =
    variableSizeBytesLong(uint32, ClientCommandCodec.codec)
}
