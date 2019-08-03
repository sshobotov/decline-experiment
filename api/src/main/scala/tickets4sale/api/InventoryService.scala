package tickets4sale.api

import java.time.LocalDate

import cats.effect.IO
import enumeratum.EnumEntry
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import tickets4sale.core.Domain._
import tickets4sale.core.{InventoryCalculator, PriceRepository, ShowRepository}

object InventoryService {
  def routes(
      showRepository:   ShowRepository[IO]
    , priceRepository:  PriceRepository[IO]
    , calc:             InventoryCalculator
  ): HttpRoutes[IO] =
    HttpRoutes.of {
      case req @ POST -> Root / "inventory" =>
        for {
          queryDate <- req.as[InventoryRequest] map { _.`show-date` }
          records   <- showRepository.find(queryDate)
          pricing   <- priceRepository.genres

          results =
            records
              .groupBy(_.genre)
              .map { case (genre, list) =>
                val today = LocalDate.now
                val shows = list map { show =>
                  val inventory = calc.inventory(show, queryDate, today)
                  val status    = calc.status(queryDate, today)
                  val price     = calc.price(show, today, pricing(genre))

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

  implicit val numberEncoder: Encoder[Int] = Encoder.encodeString.contramap(_.toString)

  implicit def enumEncoder[T <: EnumEntry]: Encoder[T] = Encoder.encodeString.contramap(_.entryName)
}
