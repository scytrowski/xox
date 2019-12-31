package xox.server.net

import cats.effect.Concurrent
import cats.syntax.functor._
import fs2.concurrent.{Enqueue, Queue}
import fs2.io.tcp.Socket
import scodec.stream.{StreamDecoder, StreamEncoder}
import scodec.{Decoder, Encoder}
import xox.core.codecs.{ClientCommandCodec, ServerCommandCodec}

object GameClient {
  def create[F[_]: Concurrent](clientId: String, socket: Socket[F]): F[GameClient[F]] =
    Client.create(clientId, socket, ServerCommandCodec.decoder, ClientCommandCodec.encoder)
}

trait Client[F[_], I, O] {
  def id: String

  def incoming: fs2.Stream[F, I]

  def outgoing: Enqueue[F, O]
}

object Client {
  def create[F[_]: Concurrent, I, O](clientId: String,
                                     socket: Socket[F],
                                     decoder: Decoder[I],
                                     encoder: Encoder[O]): F[Client[F, I, O]] =
    // fixme: Consider queue bound
    Queue.unbounded[F, O].map { queue =>
      new Client[F, I, O] {
        private val writeOutgoing =
          queue.dequeue
            .through(StreamEncoder.many(encoder).toPipeByte)
            .through(socket.writes())

        final override val id: String = clientId

        final override val incoming: fs2.Stream[F, I] =
          socket.reads(1024)
            .through(StreamDecoder.many(decoder).toPipeByte)
            .concurrently(writeOutgoing)

        final override val outgoing: Enqueue[F, O] = queue
      }
    }
}
