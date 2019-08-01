package tickets4sale.cli

import java.nio.file.Path
import java.time.LocalDate

import cats.implicits._
import cats.effect.Concurrent
import tickets4sale.core.Domain.{Genre, TicketsStatus}
import tickets4sale.core.{InventoryRules, ShowSource}
import tickets4sale.core.Syntax._
import InventoryHandler._

import scala.language.higherKinds

class InventoryHandler[F[_]: Concurrent](showDurationDays: Int, rules: InventoryRules) {
  def handle(filePath: Path, queryDate: LocalDate, showDate: LocalDate): F[InventoryResponse] = {
    val filterLeft  = showDate.minusDays(showDurationDays)
    val filterRight = showDate.plusDays(showDurationDays)

    for {
      shows   <- ShowSource.csvFailFast[F](filePath.toFile)
      matched =  shows.filter(_.openingDate.isBetween(filterLeft, filterRight))
      grouped =  matched.groupBy(_.genre)
    } yield InventoryResponse(
      grouped
        .map { case (genre, list) =>
          val shows = list map { show =>
            val inventory = rules.inventory(show, queryDate)
            val status    = rules.status(show, queryDate)

            InventoryShow(show.title, inventory.left, inventory.available, status)
          }
          InventoryGenre(genre, shows)
        }
        .toList
    )
  }
}

object InventoryHandler {
  final case class InventoryShow(title: String, tickets_left: Int, tickets_available: Int, status: TicketsStatus)

  final case class InventoryGenre(genre: Genre, shows: List[InventoryShow])

  final case class InventoryResponse(inventory: List[InventoryGenre])
}
