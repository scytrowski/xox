package xox.server.handler

import akka.actor.{ActorRef, PoisonPill}
import akka.testkit.TestProbe
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.ActorSpec
import xox.server.config.HandlerConfig
import xox.server.fixture.TestIdGenerator
import xox.server.handler.ClientManagerActor.SendCommand
import xox.server.handler.CommandManagerActor.{CommandHandlerFactory, HandleCommand}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.Try

class CommandManagerActorTest extends ActorSpec("CommandManagerActorTest") {
  "CommandManagerActor" should {

    "complete job" when {

      "worker has completed the job" in {
        val firstHandler = TestProbe()
        val secondHandler = TestProbe()
        val commandManager = system.actorOf(CommandManagerActor.props(
          HandlerConfig(1.second),
          new TestIdGenerator(),
          testCommandHandlerFactory(firstHandler.ref, secondHandler.ref)
        ))

        val firstCommandRequest = CommandRequest("123", ServerCommand.Login("abc"), testActor)
        val secondCommandRequest = CommandRequest("456", ServerCommand.Login("def"), testActor)
        commandManager ! HandleCommand(firstCommandRequest)
        commandManager ! HandleCommand(secondCommandRequest)

        firstHandler.expectMsg(HandleCommand(firstCommandRequest))
        firstHandler.ref ! PoisonPill
        secondHandler.expectMsg(HandleCommand(secondCommandRequest))
      }

      "timed out" in {
        val timeout = 500.millis
        val commandManager = system.actorOf(CommandManagerActor.props(
          HandlerConfig(timeout),
          new TestIdGenerator(),
          testCommandHandlerFactory()
        ))
        val recipient = TestProbe()
        val clientId = "123"

        val commandRequest = CommandRequest(clientId, ServerCommand.Login("abc"), recipient.ref)
        commandManager ! HandleCommand(commandRequest)

        recipient.expectNoMessage(timeout)
        recipient.expectMsg(SendCommand(clientId, ClientCommand.Timeout))
      }

    }

  }

  private def testCommandHandlerFactory(handlers: ActorRef*): CommandHandlerFactory = {
    val refs = mutable.Queue(handlers:_*)
    _ => Try(refs.dequeue()).getOrElse(testActor)
  }
}
