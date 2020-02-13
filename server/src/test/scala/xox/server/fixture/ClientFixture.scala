package xox.server.fixture

import java.net.InetSocketAddress

import akka.stream.scaladsl.{Flow, Framing, Sink, Source, Tcp}
import akka.stream.testkit.{TestPublisher, TestSubscriber}
import akka.util.ByteString
import scodec.bits.BitVector
import scodec.codecs._
import xox.core.codecs.{ClientCommandCodec, ServerCommandCodec}
import xox.core.protocol.{ClientCommand, ServerCommand}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait ClientFixture { self: StreamSpec =>
  def withClient[U](address: InetSocketAddress)(f: TestClient => U): Unit =
    withClients(address, 1)(f compose (_.head))

  def withClients[U](address: InetSocketAddress, count: Int)(
      f: List[TestClient] => U
  ): Unit = {
    implicit val ec: ExecutionContext = system.dispatcher
    val clients = Future
      .sequence {
        1.to(count).map { _ =>
          val clientFlow    = tcp.outgoingConnection(address)
          val publisher     = TestPublisher.probe[ServerCommand]()
          val commandSource = Source.fromPublisher(publisher).via(encoderFlow)
          val subscriber    = TestSubscriber.probe[ClientCommand]()
          val commandSink   = decoderFlow.to(Sink.fromSubscriber(subscriber))
          val commandFlow =
            Flow.fromSinkAndSourceCoupled(commandSink, commandSource)
          clientFlow
            .join(commandFlow)
            .run()
            .map(connection =>
              new TestClient(connection.localAddress, publisher, subscriber)
            )
        }
      }
      .futureValue
      .toList

    try {
      f(clients)
    } finally {
      clients.foreach(_.close())
    }
  }

  final protected class TestClient(
      val address: InetSocketAddress,
      private val publisher: TestPublisher.Probe[ServerCommand],
      private val subscriber: TestSubscriber.Probe[ClientCommand]
  ) {
    def send(commands: ServerCommand*): Unit =
      commands.foreach(publisher.sendNext)

    def receive: ClientCommand = receiveN(1).head

    def receiveN(count: Int): List[ClientCommand] =
      List.fill(count)(subscriber.requestNext())

    def close(): Unit = publisher.sendComplete()
  }

  private val tcp = Tcp()

  private lazy val encoderFlow = Flow[ServerCommand]
    .groupedWithin(10, 100.millis)
    .map(_.toList)
    .map(encoder.encode(_).toTry.get)
    .map(scodecBits => ByteString(scodecBits.toByteArray))
    .via(Framing.simpleFramingProtocolEncoder(1024))

  private lazy val decoderFlow = Flow[ByteString]
    .via(Framing.simpleFramingProtocolDecoder(1024))
    .map(akkaBytes => BitVector(akkaBytes.toArray))
    .mapConcat(decoder.decodeValue(_).toTry.get)

  private lazy val encoder = list(ServerCommandCodec.codec).asEncoder
  private lazy val decoder = list(ClientCommandCodec.codec).asDecoder
}
