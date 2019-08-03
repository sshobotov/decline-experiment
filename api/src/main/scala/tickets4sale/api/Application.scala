package tickets4sale.api

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import tickets4sale.core.AmsterdamInventoryCalculator
import tickets4sale.core.AmsterdamKnowledgeBase.Assumptions
import tickets4sale.core.{StaticPriceRepository, StaticShowRepository}

object Application extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val serverPort    = 8080
    val showsResource = "shows.csv"

    for {
      showRepo <- StaticShowRepository.defaultCsv[IO](showsResource, Assumptions.ShowRunDurationDays)
      service  =  InventoryService.routes(showRepo, StaticPriceRepository[IO], new AmsterdamInventoryCalculator)
      _ <-
        BlazeServerBuilder[IO]
          .bindHttp(serverPort)
          .withHttpApp(service.orNotFound)
          .resource
          .use { _ =>
            IO.delay(println(s"Server started at $serverPort")) *> IO.never
          }
    } yield
      ExitCode.Success
  }
}
