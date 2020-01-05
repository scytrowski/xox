package xox.server.fixture

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString

final class TestTcpClientActor private(address: InetSocketAddress,
                                       listener: ActorRef) extends Actor {
  import context.system

  IO(Tcp) ! Connect(address)

  override val receive: Receive = awaitConnected

  private def awaitConnected: Receive = {
    case _: Connected =>
      val connection = sender()
      connection ! Register(self)
      listener ! TestTcpClientActor.Connected
      context become awaitData(connection)
    case CommandFailed(command) =>
      listener ! TestTcpClientActor.Error(command)
      context stop self
  }

  private def awaitData(connection: ActorRef): Receive = {
    case Received(data) =>
      listener ! TestTcpClientActor.Received(data.toArray)
    case TestTcpClientActor.Send(data) =>
      connection ! Write(ByteString(data))
    case CommandFailed(command) =>
      listener ! TestTcpClientActor.Error(command)
      context stop self
  }
}

object TestTcpClientActor {
  def props(address: InetSocketAddress, listener: ActorRef): Props =
    Props(new TestTcpClientActor(address, listener))

  sealed abstract class Message
  case object Connected extends Message
  final case class Send(data: Array[Byte]) extends Message
  final case class Received(data: Array[Byte]) extends Message
  final case class Error(command: Command) extends Message
}
