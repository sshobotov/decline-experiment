package tickets4sale.core

import cats.effect.Sync
import tickets4sale.core.Domain.{Genre, GenrePricing}

import scala.language.higherKinds

trait PriceRepository[F[_]] {
  def genres: F[GenrePricing]
}

final class StaticPriceRepository[F[_]](implicit F: Sync[F]) extends PriceRepository[F] {
  private val records: GenrePricing =
    Map(
        Genre.Musicals -> 70
      , Genre.Comedy   -> 50
      , Genre.Drama    -> 40
    )

  override def genres: F[GenrePricing] = F.pure(records)
}

object StaticPriceRepository {
  def apply[F[_]: Sync] = new StaticPriceRepository[F]
}
