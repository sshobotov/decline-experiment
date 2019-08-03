package tickets4sale.cli

import cats.effect.Sync
import io.circe.{Encoder, Printer}
import io.circe.syntax._

import scala.language.higherKinds

trait Console[F[_]] {
  protected def jsonPrinter: Printer

  def putStrLn(line: String): F[Unit]

  def putJsonLn[T: Encoder](value: T): F[Unit] = putStrLn(jsonPrinter.pretty(value.asJson))
}

object Console {
  def default[F[_]: Sync](jsonPrinter: Printer = Printer.noSpaces): Console[F] =
    new StdOutConsole(jsonPrinter)
}

class StdOutConsole[F[_]](override val jsonPrinter: Printer)(implicit F: Sync[F]) extends Console[F] {
  override def putStrLn(line: String): F[Unit] = F.delay(println(line))
}
