package xox.server.net

import java.net.InetSocketAddress

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString

final case class Client(id: String,
                        address: InetSocketAddress,
                        flow: Flow[ByteString, ByteString, NotUsed])
