package xox.core.stream

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import scodec.Decoder
import scodec.bits.BitVector

object DecoderFlow {
  import scodec.codecs.list

  def apply[T](
      decoder: Decoder[T],
      framing: FramingProtocol
  ): Flow[ByteString, T, NotUsed] =
    Flow[ByteString]
      .via(framing.decoder)
      .map(akkaBytes => BitVector(akkaBytes.toArray))
      .mapConcat(bytes => list(decoder.decodeOnly).decode(bytes).toTry.get.value
      )
}
