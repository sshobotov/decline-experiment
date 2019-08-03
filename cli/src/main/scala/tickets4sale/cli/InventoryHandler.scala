package tickets4sale.cli

import java.io.File
import java.time.LocalDate

import cats.implicits._
import cats.effect.Sync
import enumeratum.EnumEntry
import io.circe.Encoder
import io.circe.generic.auto._
import tickets4sale.core.Domain.{Genre, TicketsStatus}
import tickets4sale.core.{InventoryCalculator, StaticShowRepository}

import scala.language.higherKinds

class InventoryHandler[F[_]: Sync](showDurationDays: Int, calc: InventoryCalculator)(implicit C: Console[F]) {
  import InventoryHandler._

  def handle(csvFile: File, queryDate: LocalDate, showDate: LocalDate): F[Unit] =
    for {
      showsRepo <- StaticShowRepository.defaultCsv[F](csvFile, showDurationDays)
      records   <- showsRepo.find(showDate)

      output =
        InventoryOutput(
          records
            .groupBy(_.genre)
            .map { case (genre, list) =>
              val shows = list map { show =>
                val inventory = calc.inventory(show, showDate, queryDate)
                val status    = calc.status(showDate, queryDate)

                InventoryShow(show.title, inventory.left, inventory.available, status)
              }
              InventoryGenre(genre, shows)
            }
            .toList
        )
      _ <- C.putJsonLn(output)
    } yield ()
}

object InventoryHandler {
  final case class InventoryOutput(inventory  : List[InventoryGenre])

  final case class InventoryGenre(genre: Genre, shows: List[InventoryShow])

  final case class InventoryShow(title: String, tickets_left: Int, tickets_available: Int, status: TicketsStatus)

  implicit val numberEncoder: Encoder[Int] = Encoder.encodeString.contramap(_.toString)

  implicit def enumEncoder[T <: EnumEntry]: Encoder[T] = Encoder.encodeString.contramap(_.entryName)
}
