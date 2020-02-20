package xox.server.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import scodec.bits.BitVector
import xox.core.codecs.ServerCommandCodec
import xox.core.protocol.ServerCommand
import xox.server.config.ProtocolConfig

object DecoderFlow {
  import xox.server.syntax.akka.stream._
  import xox.server.syntax.akka.bytes._
  import scodec.codecs._

  def apply(
      protocolConfig: ProtocolConfig
  )(implicit system: ActorSystem): Flow[ByteString, ServerCommand, NotUsed] = {
    implicit val adapter: LoggingAdapter = system.log
    Flow[ByteString]
      .logDebug("Incoming raw bytes", _.toHexString)
      .via(
        Framing
          .simpleFramingProtocolDecoder(protocolConfig.`max-message-length`)
      )
      .logDebug("Incoming frames", _.toHexString)
      .map(akkaBytes => BitVector(akkaBytes.toArray))
      .mapConcat(bytes => decoder.decode(bytes).toTry.get.value)
      .logDebug("Decoded commands")
  }

  private val decoder = list(ServerCommandCodec.codec).asDecoder
}
