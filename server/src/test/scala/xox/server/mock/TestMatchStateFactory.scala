package xox.server.mock

import xox.core.game.MatchParameters
import xox.server.game.{MatchState, MatchStateFactory}

final class TestMatchStateFactory(state: MatchState) extends MatchStateFactory {
  override def create(parameters: MatchParameters): MatchState = state
}
