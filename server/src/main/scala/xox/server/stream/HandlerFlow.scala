package xox.server.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.scaladsl.Flow
import xox.server.ServerState
import xox.server.handler.CommandHandler
import xox.server.net.{IncomingCommand, OutgoingCommand}

object HandlerFlow {
  import xox.server.syntax.akka.stream._

  def apply(
      handler: CommandHandler,
      initialState: ServerState
  )(
      implicit system: ActorSystem
  ): Flow[IncomingCommand, OutgoingCommand, NotUsed] = {
    implicit val adapter: LoggingAdapter = system.log
    Flow[IncomingCommand]
      .infoLog("Incoming commands")
      .pureStatefulMapConcat(initialState) { (state, command) =>
        handler.handle(command).run(state).value
      }
      .infoLog("Outgoing commands")
  }
}
