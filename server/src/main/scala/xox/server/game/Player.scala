package xox.server.game

import xox.core.game.PlayerInfo

final case class Player(id: String, nick: String, clientId: String) {
  def toInfo: PlayerInfo = PlayerInfo(id, nick)
}
