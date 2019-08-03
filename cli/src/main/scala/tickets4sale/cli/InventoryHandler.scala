package tickets4sale.cli

import java.nio.file.Path
import java.time.LocalDate

import cats.implicits._
import cats.effect.Sync
import tickets4sale.core.Domain.{Genre, TicketsStatus}
import tickets4sale.core.{InventoryCalculator, StaticShowRepository}
import InventoryHandler._
import enumeratum.EnumEntry
import io.circe.Encoder
import tickets4sale.core.AmsterdamInventoryCalculator.Assumptions

import scala.language.higherKinds

class InventoryHandler[F[_]: Sync](showDurationDays: Int, rules: InventoryCalculator) {
  def handle(filePath: Path, queryDate: LocalDate, showDate: LocalDate): F[InventoryResponse] = {
    for {
      showsRepo <- StaticShowRepository.defaultCsv[F](filePath.toFile, Assumptions.ShowRunDurationDays)
      records   <- showsRepo.find(showDate)
    } yield InventoryResponse(
      records
        .groupBy(_.genre)
        .map { case (genre, list) =>
          val shows = list map { show =>
            val inventory = rules.inventory(show, showDate, queryDate)
            val status    = rules.status(showDate, queryDate)

            InventoryShow(show.title, inventory.left, inventory.available, status)
          }
          InventoryGenre(genre, shows)
        }
        .toList
    )
  }
}

object InventoryHandler {
  final case class InventoryResponse(inventory: List[InventoryGenre])

  final case class InventoryGenre(genre: Genre, shows: List[InventoryShow])

  final case class InventoryShow(title: String, tickets_left: Int, tickets_available: Int, status: TicketsStatus)
}
