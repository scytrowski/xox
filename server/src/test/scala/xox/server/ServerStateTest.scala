package xox.server

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import xox.core.game.MatchParameters
import xox.server.ServerState.{
  CreateMatchResult,
  JoinMatchResult,
  LoginResult,
  LogoutResult
}
import xox.server.mock.{TestIdGenerator, TestMatchStateFactory}
import xox.server.game.{Match, MatchState, Player}

import scala.util.Random

class ServerStateTest extends AnyWordSpec with Matchers {
  "ServerState" when {

    "login" should {

      "successfully add new player" in {
        val state = createState(id = "456")

        state.login("abc", "123") mustBe LoginResult.Ok(
          state.copy(players = mkPlayers(Player("456", "abc", "123"))),
          "456"
        )
      }

      "inform player with requested nick is already logged in" in {
        val state = createState(players = List(Player("456", "abc", "123")))

        state.login("abc", "123") mustBe LoginResult.AlreadyLogged
      }

    }

    "logout" should {

      "successfully remove requested player" in {
        val player = Player("456", "abc", "123")
        val state  = createState(players = List(player))

        state.logout(player.id) mustBe LogoutResult.Ok(
          state.copy(players = mkPlayers())
        )
      }

      "inform requested player is unknown" in {
        val state = createState()

        state.logout("123") mustBe LogoutResult.UnknownPlayer
      }

    }

    "createMatch" should {

      "successfully create new match" in {
        val player          = Player("456", "abc", "123")
        val matchParameters = MatchParameters(4)
        val state           = createState(id = "789", players = List(player))

        state.createMatch("456", matchParameters) mustBe CreateMatchResult.Ok(
          state.copy(matches =
            mkMatches(Match.WaitingForOpponent("789", "456", matchParameters))
          ),
          "789"
        )
      }

      "inform requesting player is already in some match" in {
        val player          = Player("456", "abc", "123")
        val matchParameters = MatchParameters(4)
        val state = createState(
          players = List(player),
          matches =
            List(Match.WaitingForOpponent("789", "456", matchParameters))
        )

        state.createMatch("456", matchParameters) mustBe CreateMatchResult
          .AlreadyInMatch("789")
      }

      "inform requesting player is unknown" in {
        val state = createState()

        state.createMatch("456", MatchParameters(4)) mustBe CreateMatchResult.UnknownPlayer
      }

    }

    "joinMatch" should {

      "successfully join match" in {
        val owner           = Player("456", "abc", "123")
        val opponent        = Player("789", "def", "123")
        val matchParameters = MatchParameters(3)
        val notStartedMatch =
          Match.WaitingForOpponent("012", "456", matchParameters)
        val matchState = MatchState.create(matchParameters)
        val state = createState(
          matchState = matchState,
          players = List(owner, opponent),
          matches = List(notStartedMatch)
        )

        state.joinMatch("012", "789") mustBe JoinMatchResult.Ok(
          state.copy(matches =
            mkMatches(notStartedMatch.start(opponent.id)(_ => matchState))
          ),
          owner.id,
          matchState.ownerMark
        )
      }

      "inform requested match is already started" in {
        val owner    = Player("456", "abc", "123")
        val opponent = Player("789", "def", "123")
        val state = createState(
          players = List(owner, opponent),
          matches = List(
            Match.Ongoing(
              "012",
              owner.id,
              "345",
              MatchState.create(MatchParameters(3))
            )
          )
        )

        state.joinMatch("012", "789") mustBe JoinMatchResult.AlreadyStarted
      }

      "inform requesting player is already in match" in {
        val owner    = Player("456", "abc", "123")
        val opponent = Player("789", "def", "123")
        val state = createState(
          players = List(owner, opponent),
          matches = List(
            Match.WaitingForOpponent("012", owner.id, MatchParameters(3)),
            Match.WaitingForOpponent("345", opponent.id, MatchParameters(4))
          )
        )

        state.joinMatch("012", "789") mustBe JoinMatchResult.AlreadyInMatch(
          "345"
        )
      }

      "inform requesting player is unknown" in {
        val owner = Player("456", "abc", "123")
        val state = createState(
          players = List(owner),
          matches =
            List(Match.WaitingForOpponent("012", "456", MatchParameters(3)))
        )

        state.joinMatch("012", "789") mustBe JoinMatchResult.UnknownPlayer
      }

      "inform requested match is unknown" in {
        val owner    = Player("456", "abc", "123")
        val opponent = Player("789", "def", "123")
        val state = createState(
          players = List(owner, opponent)
        )

        state.joinMatch("012", "789") mustBe JoinMatchResult.UnknownMatch
      }

    }

  }

  private def createState(
      id: String = Random.nextString(10),
      matchState: MatchState = MatchState.create(MatchParameters(4)),
      players: List[Player] = Nil,
      matches: List[Match] = Nil
  ): ServerStateLive =
    ServerStateLive(
      mkPlayers(players: _*),
      mkMatches(matches: _*),
      new TestIdGenerator(id),
      new TestMatchStateFactory(matchState)
    )

  private def mkPlayers(players: Player*): Map[String, Player] =
    players.map(p => p.id -> p).toMap

  private def mkMatches(matches: Match*): Map[String, Match] =
    matches.map(m => m.id -> m).toMap
}
