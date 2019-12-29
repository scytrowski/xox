package xox.core.codecs

import scodec.bits.BitVector
import scodec.{Attempt, Encoder, Err, SizeBound}

object CommonCodecs {
  def selectedEncoder[A](errGen: A => Err)(pf: PartialFunction[A, Encoder[A]]): Encoder[A] =
    new Encoder[A] {
      override def encode(value: A): Attempt[BitVector] =
        pf.lift(value)
          .fold[Attempt[BitVector]](Attempt.failure(errGen(value)))(_.encode(value))

      override def sizeBound: SizeBound = SizeBound.unknown
    }
}
