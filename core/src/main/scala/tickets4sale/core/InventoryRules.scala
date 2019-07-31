package tickets4sale.core

import java.time.LocalDate

import tickets4sale.core.Domain._

trait InventoryRules {
  def status(show: Show, targetDate: LocalDate): TicketsStatus

  def inventory(show: Show, targetDate: LocalDate): Inventory

  def price(show: Show, targetDate: LocalDate, pricing: GenrePricing): PriceEuro
}

object InventoryRules {
  final class AmsterdamInventoryRules extends InventoryRules {
    import AmsterdamInventoryRules._

    override def status(show: Show, targetDate: LocalDate): TicketsStatus = {
      if (showEndDate(show) isBefore targetDate)       TicketsStatus.InThePast
      else if (saleStartDate(show) isAfter targetDate) TicketsStatus.SaleNotStarted
      else if (soldOutDate(show) isBefore targetDate)  TicketsStatus.SoldOut
      else                                             TicketsStatus.OpenForSale
    }

    override def inventory(show: Show, targetDate: LocalDate): Inventory = {
      val smallHallStartDate = show.openingDate plusDays EmpiricalKnowledge.BigHallShowDurationDays
      val ticketsLeftOnDate  =
        if (targetDate isAfter smallHallStartDate) AmsterdamRegulations.SmallHallFixedTicketsAmountPerDay
        else                                       AmsterdamRegulations.BigHallFixedTicketsAmountPerDay

      Inventory(0, ticketsLeftOnDate)
    }

    override def price(show: Show, targetDate: LocalDate, pricing: GenrePricing): PriceEuro = {
      val showPrice         = pricing(show.genre)
      val discountStartDate = show.openingDate plusDays EmpiricalKnowledge.UntilDiscountShowDurationDays

      if (targetDate isAfter discountStartDate) math.ceil(showPrice * (1 + EmpiricalKnowledge.DiscountDrop)).toInt
      else                                      showPrice
    }

    private def showEndDate(show: Show): LocalDate =
      show.openingDate plusDays EmpiricalKnowledge.ShowRunDurationDays

    private def soldOutDate(show: Show): LocalDate =
      showEndDate(show) minusDays EmpiricalKnowledge.DaysBeforeStartSoldOut

    private def saleStartDate(show: Show): LocalDate =
      show.openingDate minusDays AmsterdamRegulations.DaysBeforeShowTicketsSale
  }

  object AmsterdamInventoryRules {
    val BigHallCapacity   = 200
    val SmallHallCapacity = 100

    object EmpiricalKnowledge {
      val ShowRunDurationDays           = 100
      val BigHallShowDurationDays       = 60
      val UntilDiscountShowDurationDays = 80
      val DaysBeforeStartSoldOut        = 5

      val DiscountDrop = 0.2
    }

    object AmsterdamRegulations {
      val DaysBeforeShowTicketsSale         = 25
      val BigHallFixedTicketsAmountPerDay   = 10
      val SmallHallFixedTicketsAmountPerDay = 5
    }
  }
}
