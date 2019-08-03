package tickets4sale.core

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

import tickets4sale.core.Domain._

trait InventoryCalculator {
  def status(showDate: LocalDate, queryDate: LocalDate): TicketsStatus

  def inventory(show: Show, showDate: LocalDate, queryDate: LocalDate): Inventory

  def price(show: Show, showDate: LocalDate, price: PriceEuro): PriceEuro
}

final class AmsterdamInventoryCalculator extends InventoryCalculator {
  import AmsterdamKnowledgeBase._

  override def status(showDate: LocalDate, queryDate: LocalDate): TicketsStatus = {
    if (queryDate isAfter showDate)                      TicketsStatus.InThePast
    else if (queryDate isBefore saleStartDate(showDate)) TicketsStatus.SaleNotStarted
    else if (queryDate isBefore soldOutDate(showDate))   TicketsStatus.OpenForSale
    else                                                 TicketsStatus.SoldOut
  }

  override def inventory(show: Show, showDate: LocalDate, queryDate: LocalDate): Inventory =
    status(showDate, queryDate) match {
      case TicketsStatus.InThePast | TicketsStatus.SoldOut =>
        Inventory(0, 0)

      case TicketsStatus.SaleNotStarted =>
        Inventory(hallCapacity(show.openingDate, showDate), 0)

      case TicketsStatus.OpenForSale =>
        val daysSinceSaleStart = DAYS.between(saleStartDate(showDate), queryDate).toInt

        val capacity = hallCapacity(show.openingDate, showDate)
        val limit    = ticketsLimit(show.openingDate, showDate)

        Inventory(capacity - daysSinceSaleStart * limit, limit)
    }

  override def price(show: Show, showDate: LocalDate, price: PriceEuro): PriceEuro = {
    val daysFromOpening = DAYS.between(show.openingDate, showDate)
    if (daysFromOpening < Assumptions.UntilDiscountShowDurationDays)
      price
    else
      math.ceil(price * (1 - Assumptions.DiscountDrop)).toInt
  }

  private def isBigHallDate(openingDate: LocalDate, showDate: LocalDate): Boolean =
    DAYS.between(openingDate, showDate) < Assumptions.BigHallShowDurationDays

  private def hallCapacity(openingDate: LocalDate, showDate: LocalDate): Int =
    if (isBigHallDate(openingDate, showDate)) BigHallCapacity else SmallHallCapacity

  private def ticketsLimit(openingDate: LocalDate, showDate: LocalDate): Int =
    if (isBigHallDate(openingDate, showDate))
      Regulations.BigHallFixedTicketsAmountPerDay
    else
      Regulations.SmallHallFixedTicketsAmountPerDay

  private def soldOutDate(date: LocalDate): LocalDate =
    date minusDays Assumptions.DaysBeforeStartSoldOut

  private def saleStartDate(date: LocalDate): LocalDate =
    date minusDays Regulations.DaysBeforeShowTicketsSaleStarts
}

object AmsterdamKnowledgeBase {
  val BigHallCapacity   = 200
  val SmallHallCapacity = 100

  object Assumptions {
    val ShowRunDurationDays           = 100
    val BigHallShowDurationDays       = 60
    val UntilDiscountShowDurationDays = 80
    val DaysBeforeStartSoldOut        = 5

    val DiscountDrop = 0.2
  }

  object Regulations {
    val DaysBeforeShowTicketsSaleStarts   = 25
    val BigHallFixedTicketsAmountPerDay   = 10
    val SmallHallFixedTicketsAmountPerDay = 5
  }
}
