package xox.server.mock

import xox.core.game.{Mark, MatchInfo, MatchParameters, PlayerInfo}
import xox.server.ServerState
import xox.server.ServerState.{
  CreateMatchResult,
  JoinMatchResult,
  LoginResult,
  LogoutResult,
  MakeTurnResult
}

import scala.util.Random

class TestServerState(
    playerListResult: => List[PlayerInfo] = Nil,
    loginResult: => LoginResult =
      LoginResult.Ok(new TestServerState(), Random.nextString(10)),
    logoutResult: => LogoutResult = LogoutResult.Ok(new TestServerState()),
    matchListResult: => List[MatchInfo] = Nil,
    createMatchResult: => CreateMatchResult =
      CreateMatchResult.Ok(new TestServerState(), Random.nextString(10)),
    joinMatchResult: => JoinMatchResult = JoinMatchResult.Ok(
      new TestServerState(),
      Random.nextString(10),
      Mark.X,
      Mark.O
    ),
    makeTurnResult: => MakeTurnResult = MakeTurnResult.Ok(
      new TestServerState(),
      1,
      Random.nextString(10),
      Random.nextString(10)
    )
) extends ServerState {
  override def playerList: List[PlayerInfo] = playerListResult

  override def login(nick: String, clientId: String): LoginResult = loginResult

  override def logout(playerId: String): LogoutResult = logoutResult

  override def matchList: List[MatchInfo] = matchListResult

  override def createMatch(
      ownerId: String,
      parameters: MatchParameters
  ): CreateMatchResult = createMatchResult

  override def joinMatch(matchId: String, playerId: String): JoinMatchResult =
    joinMatchResult

  override def makeTurn(
      playerId: String,
      x: Int,
      y: Int
  ): ServerState.MakeTurnResult = makeTurnResult
}
