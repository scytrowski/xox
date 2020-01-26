package xox.core.game

import scala.util.Random

sealed abstract class Mark {
  def opposite: Mark =
    this match {
      case Mark.X => Mark.O
      case Mark.O => Mark.X
    }
}

object Mark {
  def random: Mark = Random.shuffle(X :: O :: Nil).head

  case object X extends Mark
  case object O extends Mark
}
