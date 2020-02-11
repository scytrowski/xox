package xox.server.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import scodec.bits.BitVector
import xox.core.codecs.ServerCommandCodec
import xox.core.protocol.ServerCommand
import xox.server.config.ProtocolConfig

object DecoderFlow {
  import scodec.codecs._

  def apply(protocolConfig: ProtocolConfig)(implicit system: ActorSystem): Flow[ByteString, ServerCommand, NotUsed] =
    Flow[ByteString]
      .log("Incoming Raw Bytes")
      .via(Framing.simpleFramingProtocolDecoder(protocolConfig.`max-message-length`))
      .map(akkaBytes => BitVector(akkaBytes.toArray))
      .mapConcat(bytes => decoder.decode(bytes).toTry.get.value)

  private val decoder = listOfN(uint8, ServerCommandCodec.codec).asDecoder
}
