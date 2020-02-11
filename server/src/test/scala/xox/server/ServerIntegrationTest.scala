package xox.server

import akka.util.ByteString
import org.scalatest.{Inside, TryValues}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.stream.StreamDecoder
import scodec.{Decoder, Encoder}
import xox.core.codecs.{ClientCommandCodec, ServerCommandCodec}
import xox.core.game.{Mark, MatchParameters}
import xox.core.protocol.ClientCommand.{CreateMatchOk, JoinMatchOk, LoginOk, MatchCreated, MatchStarted, PlayerLogged}
import xox.core.protocol.ServerCommand.{CreateMatch, JoinMatch, Login}
import xox.core.protocol.{ClientCommand, ServerCommand}
import xox.server.fixture.{ConfigFixture, ServerFixture, SocketFixture, StreamSpec}

class ServerIntegrationTest extends StreamSpec("ServerIntegrationTest") with ServerFixture with ConfigFixture with SocketFixture with TryValues with Inside {
  "Server" should {

    "handle single command" in {
      withServer(appConfig) { binding =>
        withConnection(binding.localAddress) { socket =>
          socket.send(encode(Login("abc")))

          expectLoginResult(socket, "abc")
        }
      }
    }

    "handle sequence of commands" in {
      withServer(appConfig) { binding =>
        withConnection(binding.localAddress) { socket =>
          socket.send(encode(Login("abc")))
          val firstPlayerId = expectLoginResult(socket, "abc")

          socket.send(encode(Login("def")))
          val secondPlayerId = expectLoginResult(socket, "def")

          val matchParameters = MatchParameters(3)
          socket.send(encode(CreateMatch(firstPlayerId, matchParameters)))
          val matchId = expectCreateMatchResult(socket, firstPlayerId, matchParameters)

          socket.send(encode(JoinMatch(secondPlayerId, matchId)))
          expectJoinMatchResult(socket, matchId, secondPlayerId)
        }
      }
    }

    "handle chunk of commands" in {
      withServer(appConfig) { binding =>
        withConnection(binding.localAddress) { socket =>
          socket.send(encode(
            Login("abc"),
            Login("def"),
            Login("ghi")
          ))

          expectLoginResults(socket, "abc", "def", "ghi")
        }
      }
    }

    "handle concurrent clients" in {
      // fixme: To be implemented
    }

  }

  private def expectLoginResult(socket: TestSocket, nick: String): String =
    expectLoginResults(socket, nick).head

  private def expectLoginResults(socket: TestSocket, nicks: String*): List[String] =
    receiveN(socket, 2 * nicks.length)
      .grouped(2)
      .toList
      .zip(nicks)
      .map { case (commands, nick) =>
        inside(commands) {
          case LoginOk(_) :: PlayerLogged(playerId, n) :: Nil if n == nick => playerId
        }
      }

  private def expectCreateMatchResult(socket: TestSocket, ownerId: String, parameters: MatchParameters): String =
    inside(receiveN(socket, 2)) {
      case CreateMatchOk(_, oId1) :: MatchCreated(matchId, oId2, p) :: Nil if oId1 == ownerId && oId2 == ownerId && p == parameters => matchId
    }

  private def expectJoinMatchResult(socket: TestSocket, matchId: String, opponentId: String): Mark =
    inside(receiveN(socket, 2)) {
      case JoinMatchOk(mId1, oId1, _) :: MatchStarted(mId2, oId2, mark) :: Nil if mId1 == matchId && oId1 == opponentId && mId2 == matchId && oId2 == opponentId => mark
    }

  private def encode(commands: ServerCommand*): ByteString =
    akkaBytes {
      encoder.encode(commands.toList).toTry.success.value
    }

  private def receiveN(socket: TestSocket, count: Int): List[ClientCommand] = {
    val commands = decode(socket.receive)
    if (commands.length >= count)
      commands.take(count)
    else
      commands ++ receiveN(socket, count - commands.length)
  }

  private def decode(akkaBytes: ByteString): List[ClientCommand] = {
    val result = decoder.decode(scodecBits(akkaBytes)).toTry.success.value
    result.value
  }

  private def akkaBytes(scodecBits: BitVector): ByteString = ByteString(scodecBits.toByteArray)

  private def scodecBits(akkaBytes: ByteString): BitVector = BitVector(akkaBytes.toArray)

  private val encoder: Encoder[List[ServerCommand]] = variableSizeBytesLong(uint32, listOfN(uint8, ServerCommandCodec.codec)).asEncoder
  private val decoder: Decoder[List[ClientCommand]] = StreamDecoder.many(variableSizeBytesLong(uint32, ClientCommandCodec.codec).asDecoder).strict.map(_.toList)
}
