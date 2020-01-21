package xox.server.game

import akka.testkit.TestProbe
import xox.server.ActorSpec
import xox.server.fixture.TestIdGenerator
import xox.server.game.MatchManagerActor.{CreateMatch, CreateMatchResponse, CreateMatchResult, Get, GetResponse, JoinMatch, JoinMatchResponse, JoinMatchResult}

class MatchManagerActorTest extends ActorSpec("MatchManagerActorTest") {
  "MatchManagerActor" when {

    "create match" should {

      "succeed" in {
        val matchId = "123"
        val ownerId = "456"
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(matchId)))

        matchManager.tell(CreateMatch(ownerId), probe.ref)
        probe.expectMsg(CreateMatchResponse(CreateMatchResult.Ok(matchId)))
        matchManager.tell(Get(matchId), probe.ref)
        probe.expectMsg(GetResponse(Some(Match.WaitingForOpponent(matchId, ownerId))))
      }

      "inform requesting player is already in some match" in {
        val matchId = "123"
        val ownerId = "456"
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(matchId)))

        matchManager.tell(CreateMatch(ownerId), probe.ref)
        probe.expectMsgType[CreateMatchResponse]
        matchManager.tell(CreateMatch(ownerId), probe.ref)
        probe.expectMsg(CreateMatchResponse(CreateMatchResult.AlreadyInMatch(matchId)))
      }

    }

    "join match" should {

      "succeed" in {
        val matchId = "123"
        val ownerId = "456"
        val opponentId = "789"
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(matchId)))

        matchManager.tell(CreateMatch(ownerId), probe.ref)
        probe.expectMsgType[CreateMatchResponse]
        matchManager.tell(JoinMatch(opponentId, matchId), probe.ref)
        probe.expectMsg(JoinMatchResponse(JoinMatchResult.Ok(ownerId)))
        matchManager.tell(Get(matchId), probe.ref)
        probe.expectMsg(GetResponse(Some(Match.Ongoing(matchId, ownerId, opponentId))))
      }

      "inform requested match is already ongoing" in {
        val matchId = "123"
        val ownerId = "456"
        val opponentId = "789"
        val anotherOpponentId = "abc"
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(matchId)))

        matchManager.tell(CreateMatch(ownerId), probe.ref)
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
        val probe = TestProbe()

        val matchManager = system.actorOf(MatchManagerActor.props(new TestIdGenerator(firstMatchId, secondMatchId)))

        matchManager.tell(CreateMatch(firstOwnerId), probe.ref)
        probe.expectMsgType[CreateMatchResponse]
        matchManager.tell(CreateMatch(secondOwnerId), probe.ref)
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
