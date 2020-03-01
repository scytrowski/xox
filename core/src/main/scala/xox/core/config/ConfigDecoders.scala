package xox.core.config

import java.net.{InetAddress, InetSocketAddress}
import cats.syntax.either._
import io.circe.Decoder

import scala.util.Try

object ConfigDecoders {
  implicit val inetSocketAddressDecoder: Decoder[InetSocketAddress] =
    Decoder.decodeString.emap { addrStr =>
      addrStr.lastIndexOf(':') match {
        case -1 => Left("Invalid Internet Socket Address format")
        case n =>
          val (hostPart, portPart) = addrStr.splitAt(n)
          for {
            host <- parseInetAddress(hostPart)
            port <- parsePort(portPart.tail)
          } yield new InetSocketAddress(host, port)
      }
    }

  private def parseInetAddress(str: String): Either[String, InetAddress] =
    Try(Option(InetAddress.getByName(str))).toEither
      .leftMap(_ => "Invalid Internet Address format")
      .flatMap {
        case Some(addr) => addr.asRight
        case None       => "Invalid Internet Address format".asLeft
      }

  private def parsePort(str: String): Either[String, Int] =
    Try(str.toInt).toEither
      .leftMap(_ => "Invalid port format")
}
