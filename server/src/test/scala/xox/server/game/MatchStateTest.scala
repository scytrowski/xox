package xox.server.game

import xox.core.game.{Mark, MatchParameters}
import xox.server.fixture.CommonSpec
import xox.server.game.BoardLike.SetResult
import xox.server.game.MatchState.PutMarkResult
import xox.server.mock.TestBoardLike
import org.scalatest.prop.TableDrivenPropertyChecks._

class MatchStateTest extends CommonSpec {
  "MatchState" when {

    "putMark" should {

      "succeed" in {
        val fieldsLeft   = 5
        val updatedBoard = new TestBoardLike(freeLeftResult = fieldsLeft)
        val state        = createState(SetResult.Ok(updatedBoard))

        state.putMark(1, 2, Mark.O) mustBe PutMarkResult.Ok(
          state.copy(board = updatedBoard, turnMark = Mark.X),
          fieldsLeft
        )
      }

      "inform outcome is a victory" in {
        val state = createState(SetResult.Victory)

        state.putMark(1, 2, Mark.O) mustBe PutMarkResult.Victory
      }

      "inform outcome is a draw" in {
        val state = createState(SetResult.Draw)

        state.putMark(1, 2, Mark.O) mustBe PutMarkResult.Draw
      }

      "inform requested incorrect field" in {
        val incorrectFieldCases = Table(
          "setResult",
          SetResult.OutOfBounds,
          SetResult.AlreadyTaken
        )

        forAll(incorrectFieldCases) { _ =>
          val state = createState(SetResult.OutOfBounds)

          state.putMark(1, 2, Mark.O) mustBe PutMarkResult.IncorrectField
        }
      }

      "inform not your turn" in {
        val state = createState(SetResult.Ok(new TestBoardLike()))

        state.putMark(1, 2, Mark.X) mustBe PutMarkResult.NotYourTurn
      }

    }

  }

  private def createState(setResult: SetResult): MatchState =
    MatchState(
      MatchParameters(3),
      Mark.X,
      Mark.O,
      new TestBoardLike(setResult = setResult)
    )
}
