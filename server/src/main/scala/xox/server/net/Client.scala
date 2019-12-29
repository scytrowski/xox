package xox.server.net

import cats.data.OptionT
import fs2.Chunk
import fs2.io.tcp.Socket
import scodec.bits.BitVector
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server._
import xox.server.codecs.{ClientCommandEncoder, ServerCommandDecoder}
import zio.Task
import zio.interop.catz._

abstract class Client {
  def id: String

  def incoming: Stream[ServerCommand]

  def send(command: ClientCommand): Task[Unit]
}

final class TcpClient(override val id: String,
                      socket: Socket[Task],
                      decoder: ServerCommandDecoder,
                      encoder: ClientCommandEncoder) extends Client {
  override def incoming: Stream[ServerCommand] =
    fs2.Stream.repeatEval(read)
      .takeWhile(_.isDefined)
      .collect { case Some(bits) => bits }
      .flatMap(decoder.decode)

  override def send(command: ClientCommand): Task[Unit] =
    for {
      bits <- encoder.encode(command)
      _    <- socket.write(Chunk.byteVector(bits.toByteVector))
    } yield ()

  private def read: Task[Option[BitVector]] =
    OptionT(socket.read(bufferSize))
      .map(_.toBitVector)
      .value

  private val bufferSize = 1024
}
