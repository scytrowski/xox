package xox.server.config

import java.net.{InetAddress, InetSocketAddress}

import cats.syntax.either._
import com.typesafe.config.{ConfigFactory, Config => TSConfig}
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.Decoder
import io.circe.config.syntax._
import zio.Task
import zio.interop.catz._

import scala.util.Try

final case class Config(server: ServerConfig)

object Config {
  import ConfigDecoders._
  import io.circe.generic.auto._

  def load(tsConfig: TSConfig = ConfigFactory.load()): Task[Config] =
    Task.fromEither(tsConfig.as[Config]("xox"))
      .tapBoth(
        error => logger.error(error)("Failed to load config"),
        _ => logger.debug("Config has been loaded")
      )

  private val logger = Slf4jLogger.getLogger[Task]
}

private object ConfigDecoders {
  implicit val inetSocketAddressDecoder: Decoder[InetSocketAddress] =
    Decoder.decodeString.emap { addrStr =>
      addrStr.lastIndexOf(':') match {
        case -1 => Left("Invalid Internet Socket Address format")
        case n  =>
          val (hostPart, portPart) = addrStr.splitAt(n)
          for {
            host <- parseInetAddress(hostPart)
            port <- parsePort(portPart.tail)
          } yield new InetSocketAddress(host, port)
      }
    }

  private def parseInetAddress(str: String): Either[String, InetAddress] =
    Try(Option(InetAddress.getByName(str)))
      .toEither
      .leftMap(_ => "Invalid Internet Address format")
      .flatMap {
        case Some(addr) => addr.asRight
        case None       => "Invalid Internet Address format".asLeft
      }

  private def parsePort(str: String): Either[String, Int] =
    Try(str.toInt).toEither
      .leftMap(_ => "Invalid port format")

}