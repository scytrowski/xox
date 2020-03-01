package xox.core.stream

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import scodec.Encoder

object EncoderFlow {
  def apply[T](
      encoder: Encoder[T],
      framing: FramingProtocol
  ): Flow[T, ByteString, NotUsed] =
    Flow[T]
      .map(encoder.encode(_).toTry.get)
      .map(scodecBytes => ByteString(scodecBytes.toByteArray))
      .via(framing.encoder)
}
