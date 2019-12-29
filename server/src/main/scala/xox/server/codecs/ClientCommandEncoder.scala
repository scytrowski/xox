package xox.server.codecs

import scodec.bits.BitVector
import xox.core.codecs.ClientCommandCodec
import xox.core.protocol.ClientCommand
import zio.Task

abstract class ClientCommandEncoder {
  def encode(command: ClientCommand): Task[BitVector]
}

final class ScodecClientCommandEncoder extends ClientCommandEncoder {
  override def encode(command: ClientCommand): Task[BitVector] =
    Task.fromTry(encoder.encode(command).toTry)

  private val encoder = ClientCommandCodec.encoder
}


