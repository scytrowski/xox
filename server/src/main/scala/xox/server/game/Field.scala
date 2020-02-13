package xox.server.game

import xox.core.game.Mark

sealed abstract class Field

object Field {
  case object Empty                  extends Field
  final case class Taken(mark: Mark) extends Field
}
