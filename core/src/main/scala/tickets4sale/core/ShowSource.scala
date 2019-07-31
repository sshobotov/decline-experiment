package tickets4sale.core

import java.io.File

import Domain.Show
import cats.effect.Sync
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import kantan.csv.enumeratum._
import kantan.csv.java8._

import scala.language.higherKinds

object ShowSource {
  def csvFile[F[_]](file: File)(implicit F: Sync[F]): F[List[Show]] =
    F.delay {
      file
        .asUnsafeCsvReader[Show](rfc.withHeader)
        .toList
    }
}
