package tickets4sale.core

import java.io.File
import java.time.LocalDate

import Domain._
import cats.implicits._
import cats.effect.Sync
import kantan.codecs.Decoder
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.java8._
import kantan.csv.generic._
import Syntax._

import scala.language.higherKinds
import scala.util.Try

trait ShowRepository[F[_]] {
  def all: F[List[Show]]

  def find(data: LocalDate): F[List[Show]]
}

final class StaticShowRepository[F[_]](records: List[Show], showRunsDays: Int)(implicit F: Sync[F])
    extends ShowRepository[F] {

  override def all: F[List[Show]] = F.pure(records)

  override def find(data: LocalDate): F[List[Show]] = F.pure {
    val (from, till) = data.midOfInterval(showRunsDays)
    records.filter(_.openingDate.isBetween(from, till))
  }
}

object StaticShowRepository {
  def defaultCsv[F[_]: Sync](resourceName: String, showRunsDays: Int): F[StaticShowRepository[F]] =
    defaultCsv[F](resourceFile(resourceName), showRunsDays)

  def defaultCsv[F[_]: Sync](file: File, showRunsDays: Int): F[StaticShowRepository[F]] =
    csvSource[F, File](file, new FailFastCsvReader, showRunsDays)

  def csvSource[F[_], T: CsvSource](
      source:            T
    , reader:            CsvReader
    , showRunsDays: Int
  )(implicit F: Sync[F]): F[StaticShowRepository[F]] =
    for {
      read    <- F.delay(reader.read(source))
      records <- F.fromEither(read)
    } yield
      new StaticShowRepository[F](records, showRunsDays)

  def resourceFile(path: String) = new File(getClass.getClassLoader.getResource(path).getFile)

  trait CsvReader {
    def read[T: CsvSource](source: T): Either[Throwable, List[Show]]
  }

  final class FailFastCsvReader extends CsvReader {
    def read[T: CsvSource](source: T): Either[Throwable, List[Show]] =
      Try {
        import CustomCsvDecoders._

        source
          .asCsvReader[Show](rfc.withoutHeader)
          .filter(isNoneEmptyLineResult)
          .map(_.toTry.get)
          .toList
      }.toEither

    private def isNoneEmptyLineResult[T](result: ReadResult[T]): Boolean =
      result match {
        case Left(DecodeError.OutOfBounds(0)) => false
        case _                                => true
      }

    private object CustomCsvDecoders {
      implicit val genreCaseInsensitiveDecoder: Decoder[String, Genre, DecodeError, codecs.type] =
        Decoder.from { raw =>
          Genre.withNameInsensitiveOption(raw) match {
            case Some(value) => Right(value)
            case _           => Left(DecodeError.TypeError(s"Can't read Genre from $raw"))
          }
        }

      implicit val stringLowercaseDecoder: Decoder[String, String, DecodeError, codecs.type] =
        Decoder.from { raw => Right(raw.toLowerCase) }
    }
  }
}
