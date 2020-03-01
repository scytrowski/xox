package xox.core.stream

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString

final case class FramingProtocol(
    encoder: Flow[ByteString, ByteString, NotUsed],
    decoder: Flow[ByteString, ByteString, NotUsed]
)

object FramingProtocol {
  def simple(maximumFrameLength: Int): FramingProtocol =
    FramingProtocol(
      Framing.simpleFramingProtocolEncoder(maximumFrameLength),
      Framing.simpleFramingProtocolDecoder(maximumFrameLength)
    )
}
