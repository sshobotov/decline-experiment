package tickets4sale.cli

import enumeratum.EnumEntry
import io.circe.Encoder

object Encoders {
  implicit val numberEncoder: Encoder[Int] = Encoder.encodeString.contramap(_.toString)

  implicit def enumEncoder[T <: EnumEntry]: Encoder[T] = Encoder.encodeString.contramap(_.entryName)
}
