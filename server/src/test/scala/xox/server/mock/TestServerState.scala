package xox.server.mock

import xox.core.game.{Mark, MatchInfo, MatchParameters}
import xox.server.ServerState
import xox.server.ServerState.{
  CreateMatchResult,
  JoinMatchResult,
  LoginResult,
  LogoutResult
}

import scala.util.Random

class TestServerState(
    loginResult: => LoginResult =
      LoginResult.Ok(new TestServerState(), Random.nextString(10)),
    logoutResult: => LogoutResult = LogoutResult.Ok(new TestServerState()),
    matchListResult: => List[MatchInfo] = Nil,
    createMatchResult: => CreateMatchResult =
      CreateMatchResult.Ok(new TestServerState(), Random.nextString(10)),
    joinMatchResult: => JoinMatchResult =
      JoinMatchResult.Ok(new TestServerState(), Random.nextString(10), Mark.X)
) extends ServerState {
  override def login(nick: String, clientId: String): LoginResult = loginResult

  override def logout(playerId: String): LogoutResult = logoutResult

  override def matchList: List[MatchInfo] = matchListResult

  override def createMatch(
      ownerId: String,
      parameters: MatchParameters
  ): CreateMatchResult = createMatchResult

  override def joinMatch(matchId: String, playerId: String): JoinMatchResult =
    joinMatchResult
}
