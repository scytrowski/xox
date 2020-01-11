package xox.server.handler

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Cancellable, Props}
import xox.core.protocol.ClientCommand
import xox.server.config.HandlerConfig
import xox.server.handler.ClientManagerActor.SendCommand
import xox.server.handler.CommandManagerActor._
import xox.server.util.IdGenerator

import scala.collection.immutable.Queue

final class CommandManagerActor private(config: HandlerConfig,
                                        idGenerator: IdGenerator,
                                        commandHandlerFactory: CommandHandlerFactory) extends Actor with ActorLogging {
  import context.dispatcher

  override val receive: Receive = awaitJob

  private def awaitJob: Receive = receiveCommands(job => proceedWithJobOrAwait(Queue(job)))()

  private def proceedWithJob(job: Job, worker: ActorRef, timeout: Cancellable, backlog: Queue[Job]): Receive =
    receiveCommands(jobForLater => proceedWithJob(job, worker, timeout, backlog.enqueue(jobForLater))) {
      case Timeout(jobId) if jobId == job.id =>
        log.warning(s"Failed to execute job $jobId due to a timeout")
        context unwatch worker
        context stop worker
        job.request.recipient ! SendCommand(job.request.clientId, ClientCommand.Timeout)
        context become proceedWithJobOrAwait(backlog)
      case Done(jobId) if jobId == job.id =>
        log.debug(s"Job ${job.id} has been executed successfully")
        timeout.cancel()
        context become proceedWithJobOrAwait(backlog)
    }

  private def receiveCommands(continue: Job => Receive)(receive: Receive = PartialFunction.empty): Receive =
    receive.orElse {
      case HandleCommand(request) =>
        val jobId = idGenerator.generate
        log.debug(s"Accepted new command request $request which will be executed as a job $jobId")
        val job = Job(jobId, request)
        context become continue(job)
    }

  private def proceedWithJobOrAwait(backlog: Queue[Job]): Receive =
    backlog.dequeueOption match {
      case Some((nextJob, backlogRest)) =>
        log.debug(s"Starting execution of job ${nextJob.id}")
        val commandHandler = commandHandlerFactory(context)
        commandHandler ! HandleCommand(nextJob.request)
        val timeoutCancellable = context.system.scheduler.scheduleOnce(config.timeout, self, Timeout(nextJob.id))
        context.watchWith(commandHandler, Done(nextJob.id))
        proceedWithJob(nextJob, commandHandler, timeoutCancellable, backlogRest)
      case None =>
        log.debug("Nothing to do at the moment. Coffee break ☕")
        awaitJob
    }
}

object CommandManagerActor {
  def props(config: HandlerConfig,
            idGenerator: IdGenerator,
            commandHandlerFactory: CommandHandlerFactory): Props =
    Props(new CommandManagerActor(config, idGenerator, commandHandlerFactory))

  type CommandHandlerFactory = ActorRefFactory => ActorRef

  final case class HandleCommand(request: CommandRequest)
  final case class Done(jobId: String)
  final case class Timeout(jobId: String)

  private final case class Job(id: String, request: CommandRequest)
}
