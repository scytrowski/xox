package xox.server.stream

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import xox.core.codecs.ClientCommandCodec
import xox.core.protocol.ClientCommand
import xox.server.config.ProtocolConfig

object EncoderFlow {
  def apply(protocolConfig: ProtocolConfig): Flow[ClientCommand, ByteString, NotUsed] =
    Flow[ClientCommand]
      .map(encoder.encode(_).toTry.get)
      .map(scodecBytes => ByteString(scodecBytes.toByteArray))
      .via(Framing.simpleFramingProtocolEncoder(protocolConfig.`max-message-length`))
      .log("Outgoing Raw Bytes")

  private val encoder = ClientCommandCodec.encoder
}
