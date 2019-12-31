package xox.server.config

import java.net.{InetAddress, InetSocketAddress}

import cats.ApplicativeError
import cats.syntax.either._
import com.typesafe.config.{ConfigFactory, Config => TSConfig}
import io.circe.Decoder
import io.circe.config.syntax._

import scala.util.Try

final case class Config(server: ServerConfig)

object Config {
  import ConfigDecoders._
  import io.circe.generic.auto._

  def load[F[_]](tsConfig: TSConfig = ConfigFactory.load())(implicit F: ApplicativeError[F, Throwable]): F[Config] =
    F.fromEither(tsConfig.as[Config]("xox"))
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