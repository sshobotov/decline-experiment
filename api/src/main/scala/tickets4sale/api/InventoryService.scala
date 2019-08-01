package tickets4sale.api

import java.time.LocalDate

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import tickets4sale.core.Domain._
import tickets4sale.core.InventoryRules

object InventoryService {
  def routes(entities: List[Show], showDurationDays: Int, rules: InventoryRules, pricing: GenrePricing): HttpRoutes[IO] =
    HttpRoutes.of {
      case req @ POST -> Root / "inventory" =>
        for {
          query   <- req.as[InventoryRequest]
          grouped =  entities.groupBy(_.genre)
          results =
            grouped
              .map { case (genre, list) =>
                val shows = list map { show =>
                  val inventory = rules.inventory(show, query.`show-date`)
                  val status    = rules.status(show, query.`show-date`)
                  val price     = rules.price(show, query.`show-date`, pricing)

                  InventoryShow(show.title, inventory.left, inventory.available, status, price)
                }
                InventoryGenre(genre, shows)
              }
              .toList
          response <- Ok(InventoryResponse(results))
        } yield response
    }

  final case class InventoryRequest(`show-date`: LocalDate)

  final case class InventoryResponse(inventory: List[InventoryGenre])

  final case class InventoryGenre(genre: Genre, shows: List[InventoryShow])

  final case class InventoryShow(
      title:             String
    , tickets_left:      Int
    , tickets_available: Int
    , status:            TicketsStatus
    , price:             PriceEuro
  )
}
