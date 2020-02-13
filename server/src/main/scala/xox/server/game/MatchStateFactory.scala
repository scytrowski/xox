package xox.server.game

import xox.core.game.MatchParameters

trait MatchStateFactory {
  def create(parameters: MatchParameters): MatchState
}

final class MatchStateFactoryLive extends MatchStateFactory {
  override def create(parameters: MatchParameters): MatchState =
    MatchState.create(parameters)
}
