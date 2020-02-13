package xox.server.game

import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import xox.core.game.Mark

class BoardTest extends AnyWordSpec with Matchers with OptionValues {
  "Board" when {

    "get" should {

      "return field" in {
        val board = Board.create(3)

        board.get(2, 1).isDefined mustBe true
      }

      "return nothing when out of bounds" in {
        val board = Board.create(3)

        board.get(-1, 1).isEmpty mustBe true
        board.get(4, 1).isEmpty mustBe true
        board.get(1, -1).isEmpty mustBe true
        board.get(1, 4).isEmpty mustBe true
      }

    }

    "set" should {

      "set selected field" in {
        val board = Board.create(3)

        board.set(1, 2, Mark.X).get(1, 2).value mustBe Field.Taken(Mark.X)
      }

      "inform out of bounds" in {
        val board = Board.create(3)

        board.set(-1, 1, Mark.X) mustBe SetResult.OutOfBounds
        board.set(4, 1, Mark.X) mustBe SetResult.OutOfBounds
        board.set(1, -1, Mark.X) mustBe SetResult.OutOfBounds
        board.set(1, 4, Mark.X) mustBe SetResult.OutOfBounds
      }

      "inform already taken" in {
        val board = Board.create(3)

        board.set(2, 1, Mark.X).set(2, 1, Mark.O) mustBe SetResult.AlreadyTaken
      }

      "inform victory" when {

        "win in row" in {
          val board = Board.create(3)

          board
            .set(0, 1, Mark.X)
            .set(1, 1, Mark.X)
            .set(2, 1, Mark.X) mustBe SetResult.Victory
        }

        "win in column" in {
          val board = Board.create(3)

          board
            .set(1, 0, Mark.X)
            .set(1, 1, Mark.X)
            .set(1, 2, Mark.X) mustBe SetResult.Victory
        }

        "win in main diagonal" in {
          val board = Board.create(3)

          board
            .set(0, 0, Mark.X)
            .set(1, 1, Mark.X)
            .set(2, 2, Mark.X) mustBe SetResult.Victory
        }

        "win in side diagonal" in {
          val board = Board.create(3)

          board
            .set(2, 0, Mark.X)
            .set(1, 1, Mark.X)
            .set(0, 2, Mark.X) mustBe SetResult.Victory
        }

      }

      "inform draw" in {
        val board = Board.create(3)

        board
          .set(0, 0, Mark.X)
          .set(1, 0, Mark.X)
          .set(2, 0, Mark.O)
          .set(0, 1, Mark.O)
          .set(1, 1, Mark.O)
          .set(2, 1, Mark.X)
          .set(0, 2, Mark.X)
          .set(1, 2, Mark.X)
          .set(2, 2, Mark.O) mustBe SetResult.Draw
      }

    }

  }
}
