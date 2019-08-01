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
import tickets4sale.core.InventoryRules.AmsterdamInventoryRules

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

      implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      implicit val tm: Timer[IO]        = IO.timer(ExecutionContext.global)

      val response =
        new InventoryHandler[IO](
            AmsterdamInventoryRules.EmpiricalKnowledge.ShowRunDurationDays
          , new AmsterdamInventoryRules
        ).handle(file, queryDate, showDate).unsafeRunSync()

      println(response.asJson)
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
