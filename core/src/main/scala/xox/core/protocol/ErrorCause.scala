package xox.core.protocol

import enumeratum.values.{ByteEnum, ByteEnumEntry}

import scala.collection.immutable

sealed abstract class ErrorCause(val value: Byte) extends ByteEnumEntry

object ErrorCause extends ByteEnum[ErrorCause] {
  case object PlayerAlreadyLogged  extends ErrorCause(1)
  case object PlayerAlreadyInMatch extends ErrorCause(2)
  case object MatchAlreadyStarted  extends ErrorCause(3)
  case object IncorrectField       extends ErrorCause(4)
  case object NotYourTurn          extends ErrorCause(5)
  case object MatchNotStarted      extends ErrorCause(6)
  case object NotInMatch           extends ErrorCause(7)
  case object MissingOpponent      extends ErrorCause(8)
  case object UnknownPlayer        extends ErrorCause(9)
  case object UnknownMatch         extends ErrorCause(10)

  override def values: immutable.IndexedSeq[ErrorCause] = findValues
}
