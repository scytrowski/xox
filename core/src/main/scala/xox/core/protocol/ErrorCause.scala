package xox.core.protocol

import enumeratum.values.{ByteEnum, ByteEnumEntry}

import scala.collection.immutable

sealed abstract class ErrorCause(val value: Byte) extends ByteEnumEntry

object ErrorCause extends ByteEnum[ErrorCause] {
  case object PlayerAlreadyLogged  extends ErrorCause(1)
  case object PlayerAlreadyInMatch extends ErrorCause(2)
  case object MatchAlreadyStarted  extends ErrorCause(3)
  case object UnknownPlayer        extends ErrorCause(4)
  case object UnknownMatch         extends ErrorCause(5)

  override def values: immutable.IndexedSeq[ErrorCause] = findValues
}
