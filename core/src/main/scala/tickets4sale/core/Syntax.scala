package tickets4sale.core

import java.io.File
import java.time.LocalDate

object Syntax {

  def resourceFile(path: String) = new File(getClass.getClassLoader.getResource(path).getFile)

  implicit class LocalDateOps(val underlying: LocalDate) extends AnyVal {
    def isBetween(left: LocalDate, right: LocalDate): Boolean =
      underlying.isAfter(left) && underlying.isBefore(right)
  }
}
