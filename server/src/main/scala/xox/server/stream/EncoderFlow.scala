package xox.server.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import xox.core.codecs.ClientCommandCodec
import xox.core.protocol.ClientCommand
import xox.server.config.ProtocolConfig

object EncoderFlow {
  import xox.server.syntax.akka.stream._
  import xox.server.syntax.akka.bytes._

  def apply(
      protocolConfig: ProtocolConfig
  )(implicit system: ActorSystem): Flow[ClientCommand, ByteString, NotUsed] = {
    implicit val adapter: LoggingAdapter = system.log
    Flow[ClientCommand]
      .map(encoder.encode(_).toTry.get)
      .map(scodecBytes => ByteString(scodecBytes.toByteArray))
      .logDebug("Outgoing raw bytes", _.toHexString)
      .via(
        Framing
          .simpleFramingProtocolEncoder(protocolConfig.`max-message-length`)
      )
      .logDebug("Outgoing frames", _.toHexString)
  }

  private val encoder = ClientCommandCodec.encoder
}
