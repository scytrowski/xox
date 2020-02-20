package xox.server.syntax.akka

import akka.util.ByteString

object bytes {
  implicit class ByteStringToHex(bytes: ByteString) {
    def toHexString: String = bytes.toList.map(_.toHexString).mkString
  }
}
