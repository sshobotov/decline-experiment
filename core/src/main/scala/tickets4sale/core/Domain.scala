package tickets4sale.core

import java.time.LocalDate

import enumeratum.EnumEntry.Uppercase
import enumeratum.{Enum, EnumEntry}

object Domain {
  sealed trait Genre extends EnumEntry with Uppercase

  object Genre extends Enum[Genre] {
    case object Drama    extends Genre
    case object Comedy   extends Genre
    case object Musicals extends Genre

    override val values = findValues
  }

  type PriceEuro    = Int
  type GenrePricing = Map[Genre, PriceEuro]

  final case class Show(title: String, openingDate: LocalDate, genre: Genre)

  sealed abstract class TicketsStatus(override val entryName: String) extends EnumEntry

  object TicketsStatus extends Enum[TicketsStatus] {
    case object SaleNotStarted extends TicketsStatus("sale not started")
    case object OpenForSale    extends TicketsStatus("open for sale")
    case object SoldOut        extends TicketsStatus("sold out")
    case object InThePast      extends TicketsStatus("in the past")

    override val values = findValues
  }

  final case class Inventory(left: Int, available: Int)
}
