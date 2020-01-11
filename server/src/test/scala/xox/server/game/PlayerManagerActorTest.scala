package xox.server.game

import akka.testkit.TestProbe
import xox.server.ActorSpec
import xox.server.fixture.TestIdGenerator
import xox.server.game.PlayerManagerActor.{Get, GetResponse, Login, LoginResponse, LoginResult, Logout, LogoutAll, LogoutResponse, LogoutResult}

class PlayerManagerActorTest extends ActorSpec("PlayerManagerActorTest") {
  "PlayerManagerActor" when {

    "login" should {

      "succeed with player with yet unknown nick" in {
        val playerManager = system.actorOf(PlayerManagerActor.props(new TestIdGenerator("456")))
        val probe = TestProbe()

        playerManager.tell(Login("123", "abc"), probe.ref)
        probe.expectMsg(LoginResponse(LoginResult.Ok("456")))
        playerManager.tell(Get("456"), probe.ref)
        probe.expectMsg(GetResponse(Some(Player("456", "abc", "123"))))
      }

      "reject player with already known nick" in {
        val playerManager = system.actorOf(PlayerManagerActor.props(new TestIdGenerator("456")))
        val probe = TestProbe()

        playerManager.tell(Login("123", "abc"), probe.ref)
        probe.expectMsg(LoginResponse(LoginResult.Ok("456")))
        playerManager.tell(Login("123", "abc"), probe.ref)
        probe.expectMsg(LoginResponse(LoginResult.AlreadyLogged))
      }

    }

    "logout" should {

      "succeed with player with known ID" in {
        val playerManager = system.actorOf(PlayerManagerActor.props(new TestIdGenerator("456")))
        val probe = TestProbe()

        playerManager.tell(Login("123", "abc"), probe.ref)
        probe.expectMsg(LoginResponse(LoginResult.Ok("456")))
        playerManager.tell(Logout("456"), probe.ref)
        probe.expectMsg(LogoutResponse(LogoutResult.Ok))
        playerManager.tell(Get("456"), probe.ref)
        probe.expectMsg(GetResponse(None))
      }

      "reject player with unknown ID" in {
        val playerManager = system.actorOf(PlayerManagerActor.props(new TestIdGenerator("456")))
        val probe = TestProbe()

        playerManager.tell(Logout("456"), probe.ref)
        probe.expectMsg(LogoutResponse(LogoutResult.NotLogged))
      }

    }

    "logout all" should {

      "logout all players connected with requested client" in {
        val playerManager = system.actorOf(PlayerManagerActor.props(new TestIdGenerator("456", "789", "ABC")))
        val probe = TestProbe()

        playerManager.tell(Login("123", "abc"), probe.ref)
        probe.expectMsg(LoginResponse(LoginResult.Ok("456")))
        playerManager.tell(Login("123", "def"), probe.ref)
        probe.expectMsg(LoginResponse(LoginResult.Ok("789")))
        playerManager.tell(Login("456", "ghi"), probe.ref)
        probe.expectMsg(LoginResponse(LoginResult.Ok("ABC")))
        playerManager ! LogoutAll("123")
        playerManager.tell(Get("456"), probe.ref)
        probe.expectMsg(GetResponse(None))
        playerManager.tell(Get("789"), probe.ref)
        probe.expectMsg(GetResponse(None))
        playerManager.tell(Get("ABC"), probe.ref)
        probe.expectMsg(GetResponse(Some(Player("ABC", "ghi", "456"))))
      }

    }

  }
}
