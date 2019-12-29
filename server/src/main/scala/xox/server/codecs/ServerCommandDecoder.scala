package xox.server.codecs

import scodec.bits.BitVector
import xox.core.codecs.ServerCommandCodec
import xox.core.protocol.ServerCommand
import xox.server._
import zio.{Task, UIO}

abstract class ServerCommandDecoder {
  def decode(bits: BitVector): Stream[ServerCommand]
}

final class ScodecServerCommandDecoder extends ServerCommandDecoder {
  override def decode(bits: BitVector): Stream[ServerCommand] =
    fs2.Stream.unfoldEval(bits)(decodeOpt)

  private def decodeOpt(bits: BitVector): Task[Option[(ServerCommand, BitVector)]] =
    if (bits.nonEmpty)
      Task.fromTry(decoder.decode(bits).toTry)
        .map(result => Some(result.value -> result.remainder))
    else
      UIO(None)

  private val decoder = ServerCommandCodec.decoder
}
