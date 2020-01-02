package xox.server.net

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.util.ByteString
import scodec.Err
import scodec.bits.BitVector
import scodec.stream.StreamDecoder
import xox.core.codecs.{ClientCommandCodec, ServerCommandCodec}
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.handler.ClientHandlerActor.{ReceivedCommand, Register, Unregister}

final class ClientActor private(id: String,
                                connection: ActorRef,
                                clientHandler: ActorRef) extends Actor with ActorLogging {
  clientHandler ! Register(id, self)

  override val receive: Receive = {
    case Received(data) =>
      decodeCommands(data) match {
        case Right(commands) =>
          commands.foreach { command =>
            log.debug(s"Received $command command from client $id")
            clientHandler ! ReceivedCommand(id, command)
          }
        case Left(error)     =>
          // fixme: Handle errors
          log.warning(s"Failed to decode commands sent by client $id due to an error: $error")
      }
    case command: ClientCommand =>
      encodeCommand(command) match {
        case Right(data) =>
          connection ! Write(data)
        case Left(error) =>
          // fixme: Handle errors
          log.error(s"Failed to encode $command command intended for client $id due to an error: $error")
      }
    case PeerClosed =>
      log.info(s"Client $id has closed the connection")
      clientHandler ! Unregister(id)
  }

  private def decodeCommands(data: ByteString): Either[Err, Seq[ServerCommand]] = {
    val scodecData = BitVector(data.toArray) // Convert to scodec native format
    decoder.decode(scodecData).toEither.map(_.value)
  }

  private def encodeCommand(command: ClientCommand): Either[Err, ByteString] =
    encoder.encode(command)
      .toEither
      .map(data => ByteString(data.toByteArray))

  private val decoder = StreamDecoder.many(ServerCommandCodec.decoder).strict
  private val encoder = ClientCommandCodec.encoder
}

object ClientActor {
  def props(id: String, connection: ActorRef, clientHandler: ActorRef): Props =
    Props(new ClientActor(id, connection, clientHandler))
}
