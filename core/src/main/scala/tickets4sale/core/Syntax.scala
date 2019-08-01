package tickets4sale.core

import java.time.LocalDate

object Syntax {
  implicit class LocalDateOps(val underlying: LocalDate) extends AnyVal {
    def isBetween(left: LocalDate, right: LocalDate): Boolean =
      underlying.isAfter(left) && underlying.isBefore(right)
  }
}
