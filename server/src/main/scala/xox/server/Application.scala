package xox.server

import akka.actor.ActorSystem
import akka.io.{IO, Tcp}
import xox.server.config.Config
import xox.server.handler.{ClientManagerActor, CommandManagerActor}
import xox.server.net.ServerActor.ClientFactory
import xox.server.net.{ClientActor, ServerActor}
import xox.server.util.UuidIdGenerator

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Application extends App {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val system: ActorSystem = ActorSystem()
  val tcp = IO(Tcp)

  Config.load() match {
    case Success(config) =>
      val idGenerator = UuidIdGenerator
      val commandManager = system.actorOf(CommandManagerActor.props(config.handler, idGenerator, ???))
      val clientHandler = system.actorOf(ClientManagerActor.props(commandManager), "client-handler")
      val clientFactory: ClientFactory = refFactory => (id, connection) => refFactory.actorOf(ClientActor.props(id, connection, clientHandler), s"client-$id")
      system.actorOf(ServerActor.props(config.server, idGenerator, tcp, clientFactory), "server")
    case Failure(ex) =>
      system.log.error(ex, "Unable to load configuration")
  }

}

//object Application extends App {
//  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
//    runF[Task].catchAll { error =>
//      zio.console.putStrLn(s"Unhandled error: ${error.getMessage}")
//        .as(-1)
//    }
//
//  private def runF[F[_]: Concurrent: ContextShift]: F[Int] = {
//    val logger = Slf4jLogger.getLogger[F]
//    fs2.Stream.eval(Config.load[F]())
//      .flatMap(config => fs2.Stream.resource(GameServer.resource[F](config.server)))
//      .evalTap(_ => logger.info("Server started"))
//      .flatMap(_.connections)
//      .evalTap(client => logger.info(s"Accepted new client ${client.id} connection"))
//      .compile
//      .drain
//      .as(0)
//  }
//}
