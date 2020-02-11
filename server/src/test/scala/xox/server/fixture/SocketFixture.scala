package xox.server.fixture

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.io.{IO, Tcp}
import akka.io.Tcp.{Close, Connect, Connected, Received, Register, Write}
import akka.testkit.TestProbe
import akka.util.ByteString

trait SocketFixture { self: ActorSpec =>
  protected def withConnection[U](address: InetSocketAddress)(f: TestSocket => U): Unit =
    withConnections(address, 1)(f compose(_.head))

  protected def withConnections[U](address: InetSocketAddress, count: Int)(f: List[TestSocket] => U): Unit = {
    val sockets = List.fill(count)(TestProbe())
      .map { probe =>
        tcp.tell(Connect(address), probe.ref)
        val clientAddress = probe.expectMsgType[Connected].localAddress
        val connection = probe.lastSender
        probe.reply(Register(probe.ref))
        new TestSocket(clientAddress, connection, probe)
      }
    try {
      f(sockets)
    } finally {
      sockets.foreach(_.close())
    }
  }

  protected final class TestSocket(val address: InetSocketAddress,
                                   private val connection: ActorRef,
                                   private val probe: TestProbe) {
    def send(data: ByteString): Unit = {
      connection.tell(Write(data, WriteDone), probe.ref)
      probe.expectMsg(WriteDone)
    }

    def receive: ByteString = probe.expectMsgType[Received].data

    def close(): Unit = connection.tell(Close, probe.ref)
  }

  private val tcp = IO(Tcp)

  private case object WriteDone extends Tcp.Event
}
