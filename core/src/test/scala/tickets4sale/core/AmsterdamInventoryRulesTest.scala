package tickets4sale.core

import java.time.LocalDate

import InventoryRules.AmsterdamInventoryRules
import Domain._
import utest._

object AmsterdamInventoryRulesTest extends TestSuite {
  val tests = Tests {
    test("AmsterdamInventoryRules") {
      val show = Show("test title", LocalDate.now().minusDays(20), Genre.Comedy)

      test("should correctly predict inventory") {
        val expect = Inventory(100, 10)
        val actual = new AmsterdamInventoryRules().inventory(show, LocalDate.now())

        assert(expect == actual)
      }
    }
  }
}
