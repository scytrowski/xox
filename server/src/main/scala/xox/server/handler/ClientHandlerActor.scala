package xox.server.handler

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.handler.ClientHandlerActor.{CommandHandlerFactory, ReceivedCommand, Register, SendCommand, Unregister}
import xox.server.handler.CommandHandlerActor.HandleCommand

final class ClientHandlerActor private(commandHandlerFactory: CommandHandlerFactory) extends Actor with ActorLogging {
  override def receive: Receive = handleClients(Map.empty)

  private def handleClients(clients: Map[String, ActorRef]): Receive = {
    case Register(clientId, clientRef) =>
      clients.get(clientId) match {
        case None    =>
          log.debug(s"Registered new client $clientId")
          context become handleClients(clients + (clientId -> clientRef))
        case Some(_) =>
          // fixme: Handle error
          log.warning(s"Attempted to register client $clientId which is already registered")
      }
    case Unregister(clientId) =>
      clients.get(clientId) match {
        case Some(_) =>
          log.debug(s"Unregistered client $clientId")
          context become handleClients(clients - clientId)
        case None    =>
          // fixme: Handle error
          log.warning(s"Attempted to unregister unknown client $clientId")
      }
    case ReceivedCommand(clientId, command) =>
      clients.get(clientId) match {
        case Some(_) =>
          log.debug(s"Going to handle $command command received from client $clientId")
          val commandHandler = commandHandlerFactory(context)(self)
          commandHandler ! HandleCommand(clientId, command)
        case None    =>
          // fixme: Handle error
          log.warning(s"Received $command command from unknown client $clientId")
      }
    case SendCommand(clientId, command) =>
      clients.get(clientId) match {
        case Some(client) =>
          log.debug(s"Sending $command command to client $clientId")
          client ! command
        case None         =>
          // fixme: Handle errors
          log.warning(s"Attempted to send $command command to an unknown client $clientId")
      }
  }
}

object ClientHandlerActor {
  type CommandHandlerFactory = ActorRefFactory => ActorRef => ActorRef

  final case class Register(clientId: String, clientRef: ActorRef)

  final case class Unregister(clientId: String)

  final case class ReceivedCommand(clientId: String, command: ServerCommand)

  final case class SendCommand(clientId: String, command: ClientCommand)

  def props(commandHandlerFactory: CommandHandlerFactory): Props =
    Props(new ClientHandlerActor(commandHandlerFactory))
}