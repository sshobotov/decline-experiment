package tickets4sale.api

import java.io.File

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import tickets4sale.core.InventoryRules.AmsterdamInventoryRules
import tickets4sale.core.{ShowsSource, Syntax}
import tickets4sale.core.Domain.{Genre, GenrePricing}

object Application extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      shows   <- ShowsSource.csvFailFast[IO, File](Syntax.resourceFile("example.csv"))
      pricing: GenrePricing =  Map(
          Genre.Musicals -> 70
        , Genre.Comedy   -> 50
        , Genre.Drama    -> 40
      )
      service =  InventoryService.routes(
          shows
        , AmsterdamInventoryRules.EmpiricalKnowledge.ShowRunDurationDays
        , new AmsterdamInventoryRules
        , pricing
      )
      _       <-
        BlazeServerBuilder[IO]
          .bindHttp(8080)
          .withHttpApp(service.orNotFound)
          .resource
          .use(_ => IO.never)
    } yield ExitCode.Success
}
