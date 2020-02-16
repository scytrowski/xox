package xox.core.codecs

import scodec.bits.BitVector
import scodec.{Attempt, Codec, Decoder, Encoder, Err, SizeBound}
import enumeratum.values.{ValueEnum, ValueEnumEntry}

import scala.reflect.ClassTag

object CommonCodecs {
  import scodec.codecs._

  def selectedEncoder[A](
      errGen: A => Err
  )(pf: PartialFunction[A, Encoder[A]]): Encoder[A] =
    new Encoder[A] {
      override def encode(value: A): Attempt[BitVector] =
        pf.lift(value)
          .fold[Attempt[BitVector]](Attempt.failure(errGen(value)))(
            _.encode(value)
          )

      override def sizeBound: SizeBound = SizeBound.unknown
    }

  def valueEnumCodec[V, E <: ValueEnumEntry[V]: ClassTag](
      enum: ValueEnum[V, E],
      valueCodec: Codec[V]
  ): Codec[E] =
    Codec(
      valueEnumEncoder(enum, valueCodec),
      valueEnumDecoder(enum, valueCodec)
    )

  def valueEnumEncoder[V, E <: ValueEnumEntry[V]](
      enum: ValueEnum[V, E],
      valueEncoder: Encoder[V]
  ): Encoder[E] =
    valueEncoder.contramap(_.value)

  def valueEnumDecoder[V, E <: ValueEnumEntry[V]: ClassTag](
      enum: ValueEnum[V, E],
      valueDecoder: Decoder[V]
  ): Decoder[E] =
    valueDecoder.emap { value =>
      Attempt.fromEither {
        enum.valuesToEntriesMap
          .get(value)
          .toRight(
            Err(
              s"Cannot decode instance of ${implicitly[ClassTag[E]].runtimeClass.getName} from value $value"
            )
          )
      }
    }

  val string16: Codec[String] = variableSizeBytes(uint16, ascii)
}
