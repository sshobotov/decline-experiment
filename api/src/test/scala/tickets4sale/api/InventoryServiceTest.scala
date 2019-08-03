package tickets4sale.api

import java.time.LocalDate

import cats.effect.{ContextShift, IO, Timer}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityEncoder._
import tickets4sale.api.InventoryService.InventoryRequest
import tickets4sale.core.Domain._
import tickets4sale.core.{Domain, InventoryCalculator, StaticPriceRepository, StaticShowRepository}
import utest._

import scala.concurrent.ExecutionContext

object InventoryServiceTest extends TestSuite {
  val tests = Tests {
    test("InventoryService.routes should return json of expected format") {
      implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      implicit val tm: Timer[IO]        = IO.timer(ExecutionContext.global)

      val shows   = List(Show("cats", LocalDate.parse("2018-06-01"), Genre.Musical))
      val service = InventoryService.routes(
          new StaticShowRepository[IO](shows, 20)
        , StaticPriceRepository[IO]
        , dummyCalculator
      )

      val request  =
        Request[IO](method = Method.POST, uri = uri"/inventory")
          .withEntity(InventoryRequest(LocalDate.parse("2018-06-20")))
      val response = service.orNotFound.run(request).unsafeRunSync()

      val expect = """{"inventory":[{"genre":"musical","shows":[{"title":"cats","tickets_left":"100","tickets_available":"5","status":"open for sale","price":"50"}]}]}"""
      val actual = response.bodyAsText.compile.toList.unsafeRunSync().mkString

      assert(expect == actual)
    }
  }

  private def dummyCalculator =
    new InventoryCalculator {
      override def status(showDate: LocalDate, queryDate: LocalDate): TicketsStatus = TicketsStatus.OpenForSale

      override def inventory(show: Domain.Show, showDate: LocalDate, queryDate: LocalDate): Domain.Inventory =
        Inventory(100, 5)

      override def price(show: Domain.Show, showDate: LocalDate, price: PriceEuro): PriceEuro = 50
    }
}
