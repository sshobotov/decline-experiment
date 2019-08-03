package tickets4sale.cli

import java.time.LocalDate

import cats.implicits._
import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, IO, Timer}
import io.circe.Printer
import tickets4sale.core.Domain.{Inventory, PriceEuro, TicketsStatus}
import tickets4sale.core.{Domain, Files, InventoryCalculator}
import utest._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

object InventoryHandlerTest extends TestSuite {
  val tests = Tests {
    test("InventoryHandler.handle should print data with expected format") {
      implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      implicit val tm: Timer[IO]        = IO.timer(ExecutionContext.global)

      val linesAccumulator              = Ref.unsafe[IO, ListBuffer[String]](ListBuffer.empty)
      implicit val console: Console[IO] = linesAccumulatingConsole(linesAccumulator)

      val csv    = Files.resource("example.csv")
      val output =
        for {
          _     <- new InventoryHandler[IO](20, dummyCalculator).handle(csv, LocalDate.parse("2018-08-02"), LocalDate.parse("2018-08-07"))
          lines <- linesAccumulator.get
        } yield lines.mkString

      val expect = """{"inventory":[{"genre":"drama","shows":[{"title":"everyman","tickets_left":"100","tickets_available":"5","status":"open for sale"}]}]}"""
      val actual = output.unsafeRunSync()

      assert(expect == actual)
    }
  }

  private def linesAccumulatingConsole(acc: Ref[IO, ListBuffer[String]]) =
    new Console[IO] {
      override protected def jsonPrinter: Printer = Printer.noSpaces

      override def putStrLn(line: String): IO[Unit] = acc.update(_ += line)
    }

  private def dummyCalculator =
    new InventoryCalculator {
      override def status(showDate: LocalDate, queryDate: LocalDate): TicketsStatus = TicketsStatus.OpenForSale

      override def inventory(show: Domain.Show, showDate: LocalDate, queryDate: LocalDate): Domain.Inventory =
        Inventory(100, 5)

      override def price(show: Domain.Show, showDate: LocalDate, price: PriceEuro): PriceEuro = 50
    }
}
