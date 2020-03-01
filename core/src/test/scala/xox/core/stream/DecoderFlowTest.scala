package xox.core.stream

import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import scodec.Encoder
import scodec.bits.BitVector
import xox.core.codec.ServerCommandCodec
import xox.core.fixture.StreamSpec
import xox.core.game.MatchParameters
import xox.core.protocol.ServerCommand
import xox.core.protocol.ServerCommand.{CreateMatch, JoinMatch, Login}

class DecoderFlowTest
    extends StreamSpec("DecoderFlowTest")
    with EitherValues
    with ScalaFutures {
  import scodec.codecs._

  "DecoderFlow" should {

    "decode server commands" in {
      val command1 = Login("abc")
      val command2 = CreateMatch("123", MatchParameters(3))
      val command3 = JoinMatch("456", "789")
      val command4 = Login("def")
      val command5 = CreateMatch("012", MatchParameters(5))

      val bytes1 = encode(command1, command2, command3)
      val bytes2 = encode(command4, command5)

      Source(List(bytes1, bytes2))
        .via(
          DecoderFlow(ServerCommandCodec.decoder, FramingProtocol.simple(1024))
        )
        .runWith(Sink.seq)
        .futureValue must contain theSameElementsInOrderAs List(
        command1,
        command2,
        command3,
        command4,
        command5
      )
    }

  }

  private def encode(commands: ServerCommand*): ByteString =
    encoder
      .encode(commands.toList)
      .toEither
      .map(akkaBytes)
      .right
      .value

  private def akkaBytes(scodecBytes: BitVector): ByteString =
    ByteString(scodecBytes.toByteArray)

  private val encoder: Encoder[List[ServerCommand]] =
    variableSizeBytesLong(uint32, list(ServerCommandCodec.codec))
}
