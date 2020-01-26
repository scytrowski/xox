package xox.server.game

import akka.testkit.TestProbe
import xox.core.game.MatchParameters
import xox.server.ActorSpec
import xox.server.fixture.TestIdGenerator
import xox.server.game.MatchManagerActor.{CreateMatch, CreateMatchResponse, CreateMatchResult, Get, GetResponse, JoinMatch, JoinMatchResponse, JoinMatchResult}

class MatchManagerActorTest extends ActorSpec("MatchManagerActorTest") {
  "MatchManagerActor" when {

    "create match" should {

      "succeed" in {
        val matchId = "123"
        val ownerId = "456"
        val parameters = MatchParameters(5)
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(matchId)))

        matchManager.tell(CreateMatch(ownerId, parameters), probe.ref)
        probe.expectMsg(CreateMatchResponse(CreateMatchResult.Ok(matchId)))
        matchManager.tell(Get(matchId), probe.ref)
        probe.expectMsg(GetResponse(Some(Match.WaitingForOpponent(matchId, ownerId, parameters))))
      }

      "inform requesting player is already in some match" in {
        val matchId = "123"
        val ownerId = "456"
        val parameters = MatchParameters(5)
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(matchId)))

        matchManager.tell(CreateMatch(ownerId, parameters), probe.ref)
        probe.expectMsgType[CreateMatchResponse]
        matchManager.tell(CreateMatch(ownerId, parameters), probe.ref)
        probe.expectMsg(CreateMatchResponse(CreateMatchResult.AlreadyInMatch(matchId)))
      }

    }

    "join match" should {

      "succeed" in {
        val matchId = "123"
        val ownerId = "456"
        val opponentId = "789"
        val parameters = MatchParameters(5)
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(matchId)))

        matchManager.tell(CreateMatch(ownerId, parameters), probe.ref)
        probe.expectMsgType[CreateMatchResponse]
        matchManager.tell(JoinMatch(opponentId, matchId), probe.ref)
        probe.expectMsgPF() { case JoinMatchResponse(JoinMatchResult.Ok(id, _)) if id == ownerId => }
        matchManager.tell(Get(matchId), probe.ref)
        // fixme: Stronger assertion to be added
        probe.expectMsgPF() { case GetResponse(Some(_)) => }
      }

      "inform requested match is already ongoing" in {
        val matchId = "123"
        val ownerId = "456"
        val opponentId = "789"
        val anotherOpponentId = "abc"
        val parameters = MatchParameters(5)
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(matchId)))

        matchManager.tell(CreateMatch(ownerId, parameters), probe.ref)
        probe.expectMsgType[CreateMatchResponse]
        matchManager.tell(JoinMatch(opponentId, matchId), probe.ref)
        probe.expectMsgType[JoinMatchResponse]
        matchManager.tell(JoinMatch(anotherOpponentId, matchId), probe.ref)
        probe.expectMsg(JoinMatchResponse(JoinMatchResult.AlreadyOngoing))
      }

      "inform requesting player is already in some match" in {
        val firstMatchId = "123"
        val secondMatchId = "456"
        val firstOwnerId = "789"
        val secondOwnerId = "abc"
        val parameters = MatchParameters(5)
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(firstMatchId, secondMatchId)))

        matchManager.tell(CreateMatch(firstOwnerId, parameters), probe.ref)
        probe.expectMsgType[CreateMatchResponse]
        matchManager.tell(CreateMatch(secondOwnerId, parameters), probe.ref)
        probe.expectMsgType[CreateMatchResponse]
        matchManager.tell(JoinMatch(secondOwnerId, firstMatchId), probe.ref)
        probe.expectMsg(JoinMatchResponse(JoinMatchResult.AlreadyInMatch(secondMatchId)))
      }

      "inform requested match does not exist" in {
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator()))

        matchManager.tell(JoinMatch("123", "456"), probe.ref)
        probe.expectMsg(JoinMatchResponse(JoinMatchResult.MatchNotExist))
      }

    }

  }
}
