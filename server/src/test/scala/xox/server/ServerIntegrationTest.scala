package xox.server

import org.scalatest.{Inside, OptionValues, TryValues}
import xox.core.game.{Mark, MatchParameters}
import xox.core.protocol.ClientCommand._
import xox.core.protocol.ServerCommand.{CreateMatch, JoinMatch, Login}
import xox.server.fixture.{ClientFixture, ConfigFixture, ServerFixture, StreamSpec}
import xox.server.syntax.list._

import scala.util.Random

class ServerIntegrationTest extends StreamSpec("ServerIntegrationTest") with ServerFixture with ConfigFixture with ClientFixture with TryValues with OptionValues with Inside {
  "Server" should {

    "handle single command" in {
      val playerId = randomString()

      withServer(appConfig, randomString(), playerId) { binding =>
        withClient(binding.localAddress) { client =>
          client.send(Login("abc"))

          expectLoginResult(client, "abc") mustBe playerId
        }
      }
    }

    "handle sequence of commands" in {
      val firstPlayerId = randomString()
      val secondPlayerId = randomString()
      val matchId = randomString()

      withServer(appConfig, randomString(), firstPlayerId, secondPlayerId, matchId) { binding =>
        withClient(binding.localAddress) { client =>
          client.send(Login("abc"))
          expectLoginResult(client, "abc") mustBe firstPlayerId

          client.send(Login("def"))
          expectLoginResult(client, "def") mustBe secondPlayerId

          val matchParameters = MatchParameters(3)
          client.send(CreateMatch(firstPlayerId, matchParameters))
          expectCreateMatchResult(client, firstPlayerId, matchParameters) mustBe matchId

          client.send(JoinMatch(secondPlayerId, matchId))
          expectJoinMatchResult(client, matchId, secondPlayerId)
        }
      }
    }

    "handle chunk of commands" in {
      val firstPlayerId = randomString()
      val secondPlayerId = randomString()
      val thirdPlayerId = randomString()

      withServer(appConfig, randomString(), firstPlayerId, secondPlayerId, thirdPlayerId) { binding =>
        withClient(binding.localAddress) { client =>
          client.send(
            Login("abc"),
            Login("def"),
            Login("ghi")
          )

          expectLoginResults(client, "abc", "def", "ghi") must contain theSameElementsInOrderAs List(firstPlayerId, secondPlayerId, thirdPlayerId)
        }
      }
    }

    "handle concurrent clients" in {
      val clientsCount = 2
      val clientIds = randomStrings(clientsCount)
      val playerIds = randomStrings(clientsCount)

      withServer(appConfig, clientIds ++ playerIds:_*) { binding =>
        withClients(binding.localAddress, clientsCount) { clients =>
          val nicks = randomStrings(clientsCount)

          clients.zip(nicks).foreach { case (client, nick) => client.send(Login(nick)) }
          clients.foreach { client =>
            val commands = client.receiveN(clientsCount + 1)
            val loginOk = commands.collect { case l: LoginOk => l }.single.value
            val playerLogged = commands.collect { case l: PlayerLogged => l }

            playerIds must contain (loginOk.playerId)
            playerLogged.map(_.playerId) must contain theSameElementsAs playerIds
            playerLogged.map(_.nick) must contain theSameElementsAs nicks
          }
        }
      }
    }

  }

  private def expectLoginResult(client: TestClient, nick: String): String =
    expectLoginResults(client, nick).head

  private def expectLoginResults(client: TestClient, nicks: String*): List[String] =
    client.receiveN(2 * nicks.length)
      .grouped(2)
      .toList
      .zip(nicks)
      .map { case (commands, nick) =>
        inside(commands) {
          case LoginOk(_) :: PlayerLogged(playerId, n) :: Nil if n == nick => playerId
        }
      }

  private def expectCreateMatchResult(client: TestClient, ownerId: String, parameters: MatchParameters): String =
    inside(client.receiveN(2)) {
      case CreateMatchOk(_, oId1) :: MatchCreated(matchId, oId2, p) :: Nil if oId1 == ownerId && oId2 == ownerId && p == parameters => matchId
    }

  private def expectJoinMatchResult(client: TestClient, matchId: String, opponentId: String): Mark =
    inside(client.receiveN(2)) {
      case JoinMatchOk(mId1, oId1, _) :: MatchStarted(mId2, oId2, mark) :: Nil if mId1 == matchId && oId1 == opponentId && mId2 == matchId && oId2 == opponentId => mark
    }

  private def randomStrings(count: Int, length: Int = 10) = List.fill(count)(randomString(length))

  private def randomString(length: Int = 10) = List.fill(length)(Random.nextPrintableChar()).mkString
}
