package tickets4sale.core

import java.time.LocalDate

import AmsterdamKnowledgeBase.{Assumptions, Regulations}
import Domain._
import utest._

object AmsterdamInventoryCalculatorTest extends TestSuite {
  val tests = Tests {
    test("AmsterdamInventoryCalculator") {
      val daysShowAlreadyRunning = 20

      val beforeSaleStartsDay = fromNow(Regulations.DaysBeforeShowTicketsSaleStarts + 1)
      val discountStartDay    = fromNow(Assumptions.UntilDiscountShowDurationDays - daysShowAlreadyRunning)
      val soldOutDay          = fromNow(Assumptions.DaysBeforeStartSoldOut - 1)

      val show = Show("test comedy", LocalDate.now minusDays daysShowAlreadyRunning, Genre.Comedy)
      val calc = new AmsterdamInventoryCalculator

      test("status should be `in the past` for the show from the past") {
        val expect = TicketsStatus.InThePast
        val actual = calc.status(beforeNow(5), LocalDate.now)

        assert(expect == actual)
      }

      test("status should be `sold out` for the show after sold out day") {
        val expect = TicketsStatus.SoldOut
        val actual = calc.status(soldOutDay, LocalDate.now)

        assert(expect == actual)
      }

      test("status should be `sale not started` for the show from the future") {
        val expect = TicketsStatus.SaleNotStarted
        val actual = calc.status(beforeSaleStartsDay, LocalDate.now)

        assert(expect == actual)
      }

      test("status should be `open for sale` for the show right on a sale") {
        val expect = TicketsStatus.OpenForSale
        val actual = calc.status(fromNow(10), LocalDate.now)

        assert(expect == actual)
      }

      test("inventory should correctly predict inventory if date matches small hall and sale didn't start") {
        val expect = Inventory(100, 0)
        val actual = calc.inventory(show, fromNow(60), LocalDate.now)

        assert(expect == actual)
      }

      test("inventory should correctly predict inventory if date matches big hall and sale already start") {
        val expect = Inventory(50, 10)
        val actual = calc.inventory(show, fromNow(10), LocalDate.now)

        assert(expect == actual)
      }

      test("inventory should correctly predict inventory for the show in the past") {
        val expect = Inventory(0, 0)
        val actual = calc.inventory(show, beforeNow(5), LocalDate.now)

        assert(expect == actual)
      }

      val price: PriceEuro = 100

      test("price should calculate normal price for ongoing show") {
        val expect = 100
        val actual = calc.price(show, LocalDate.now, price)

        assert(expect == actual)
      }

      test("price should calculate normal price for future show") {
        val expect = 100
        val actual = calc.price(show, beforeNow(100), price)

        assert(expect == actual)
      }

      test("price should calculate discount price for ongoing show near the end dates") {
        val expect = 80
        val actual = calc.price(show, discountStartDay, price)

        assert(expect == actual)
      }

      test("price should calculate discount price for the show in the past") {
        val expect = 80
        val actual = calc.price(show, fromNow(Assumptions.ShowRunDurationDays), price)

        assert(expect == actual)
      }
    }
  }

  private def fromNow(days: Int) = LocalDate.now plusDays days

  private def beforeNow(days: Int) = LocalDate.now minusDays days
}
