package tickets4sale.cli

import java.nio.file.Path
import java.time.LocalDate
import java.time.format

import ArgumentExt._
import cats.data.{Validated, ValidatedNel}
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import com.monovore.decline._
import io.circe.syntax._
import tickets4sale.core.AmsterdamInventoryCalculator
import tickets4sale.core.AmsterdamInventoryCalculator.Assumptions

import scala.concurrent.ExecutionContext

object Application extends CommandApp(
    name   = "inventory"
  , header = "Inventory CLI tools"
  , main   = {
    val fileArg      = Opts.argument[Path](metavar = "file")
    val queryDateArg = Opts.argument[LocalDate](metavar = "query-date")
    val showDateArg  = Opts.argument[LocalDate](metavar = "show-date")

    (fileArg, queryDateArg, showDateArg) mapN { (file, queryDate, showDate) =>
      import io.circe.generic.auto._
      import InventoryHandler._

      implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      implicit val tm: Timer[IO]        = IO.timer(ExecutionContext.global)

      val handler  = new InventoryHandler[IO](Assumptions.ShowRunDurationDays, new AmsterdamInventoryCalculator)
      val response = handler.handle(file, queryDate, showDate).unsafeRunSync()

      println(response)
    }
  }
)

object ArgumentExt {
  implicit val dateArgument: Argument[LocalDate] = new Argument[LocalDate] {
    override def read(string: String): ValidatedNel[String, LocalDate] =
      try Validated.valid(LocalDate.parse(string))
      catch {
        case _: format.DateTimeParseException =>
          Validated.invalidNel(s"Invalid date format, expected YYYY-MM-DD: $string")
      }

    override def defaultMetavar: String = "date"
  }
}
