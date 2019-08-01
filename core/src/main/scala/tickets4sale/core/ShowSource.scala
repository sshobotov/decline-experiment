package tickets4sale.core

import java.time.LocalDate

import Domain._
import cats.effect.Sync
import kantan.codecs.Decoder
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.java8._
import kantan.csv.generic._

import scala.language.higherKinds

object ShowSource {
  def csvFailFast[F[_], T: CsvSource](source: T)(implicit F: Sync[F]): F[List[Show]] =
    F.delay {
      import CustomDecoders._

      implicit val showDecoder = customizedShowRowDecoder

      source
        .asCsvReader[Show](rfc.withoutHeader)
        .filter(isNoneEmptyLineResult)
        .map(_.toTry.get)
        .toList
    }

  private def isNoneEmptyLineResult[T](result: ReadResult[T]): Boolean =
    result match {
      case Left(DecodeError.OutOfBounds(0)) => false
      case _                                => true
    }

  object CustomDecoders {
    implicit val genreCaseInsensitiveDecoder: Decoder[String, Genre, DecodeError, codecs.type] =
      Decoder.from { raw =>
        Genre.withNameInsensitiveOption(raw) match {
          case Some(value) => Right(value)
          case _           => Left(DecodeError.TypeError(s"Can't read Genre from $raw"))
        }
      }

    def customizedShowRowDecoder(implicit generic: RowDecoder[Show]): RowDecoder[Show] =
      generic map { record => record.copy(title = record.title.toLowerCase) }
  }
}
