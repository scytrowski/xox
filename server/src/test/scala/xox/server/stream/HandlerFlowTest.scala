package xox.server.stream

import akka.stream.scaladsl.{Sink, Source}
import cats.data.State
import org.scalatest.concurrent.ScalaFutures
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.mock.TestServerState
import xox.server.ServerState
import xox.server.fixture.StreamSpec
import xox.server.handler.CommandHandler
import xox.server.net.OutgoingCommand.{Broadcast, Private}
import xox.server.net.{IncomingCommand, OutgoingCommand}

class HandlerFlowTest extends StreamSpec("HandlerFlowTest") with ScalaFutures {
  "HandlerFlow" should {

    "handle incoming commands producing outgoing" in {
      val outgoing = List(
        Private("123", ClientCommand.LoginOk("456")),
        Broadcast(ClientCommand.PlayerLogged("456", "abc"))
      )
      val flow = HandlerFlow(new TestCommandHandler(outgoing), new TestServerState())

      Source.single(IncomingCommand("123", ServerCommand.Login("abc")))
        .via(flow)
        .runWith(Sink.seq)
        .futureValue must contain theSameElementsInOrderAs outgoing
    }

  }

  private final class TestCommandHandler(outgoing: List[OutgoingCommand]) extends CommandHandler {
    override def handle(command: IncomingCommand): State[ServerState, List[OutgoingCommand]] =
      State(_ -> outgoing)
  }
}
