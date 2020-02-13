package xox.server.stream

import akka.NotUsed
import akka.stream.scaladsl.Flow
import xox.server.ServerState
import xox.server.handler.CommandHandler
import xox.server.net.{IncomingCommand, OutgoingCommand}
import xox.server.syntax.akka.stream._

object HandlerFlow {
  def apply(
      handler: CommandHandler,
      initialState: ServerState
  ): Flow[IncomingCommand, OutgoingCommand, NotUsed] =
    Flow[IncomingCommand]
      .log("Incoming Commands")
      .pureStatefulMapConcat(initialState) { (state, command) =>
        handler.handle(command).run(state).value
      }
      .log("Outgoing Commands")
}
