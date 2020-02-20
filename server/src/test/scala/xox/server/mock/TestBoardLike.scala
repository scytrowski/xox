package xox.server.mock

import xox.core.game.Mark
import xox.server.game.{BoardLike, Field}
import xox.server.game.BoardLike.SetResult

final class TestBoardLike(
    getResult: => Option[Field] = Some(Field.Empty),
    setResult: => SetResult = SetResult.Ok(new TestBoardLike()),
    freeLeftResult: => Int = 1
) extends BoardLike {
  override def get(x: Int, y: Int): Option[Field] = getResult

  override def set(x: Int, y: Int, mark: Mark): SetResult = setResult

  override def freeLeft: Int = freeLeftResult
}
