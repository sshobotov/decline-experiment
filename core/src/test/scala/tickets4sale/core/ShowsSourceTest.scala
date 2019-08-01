package tickets4sale.core

import java.io.File
import java.time.LocalDate

import Domain._
import cats.effect.IO
import kantan.csv.DecodeError
import utest._

object ShowsSourceTest extends TestSuite {
  val tests = Tests {
    test("ShowsSource") {
      test("csvFailFast should reliably extract data with known CSV format even with empty lines") {
        val expect = List(
            Show("1984", LocalDate.parse("2019-10-14"), Genre.Drama)
          , Show("39 steps, the ", LocalDate.parse("2019-11-10"), Genre.Comedy)
          , Show("a midsummer night’s dream - in new orleans", LocalDate.parse("2020-04-28"), Genre.Drama)
        )
        val actual =
          ShowsSource
            .csvFailFast[IO, File](Syntax.resourceFile("example.csv"))
            .unsafeRunSync()

        assert(expect == actual)
      }

      test("csvFailFast should fail for unexpected input data") {
        intercept[DecodeError.TypeError] {
          ShowsSource
            .csvFailFast[IO, String]("""CATS,2019-08-15,TEST""")
            .unsafeRunSync()
        }
      }

      test("csvFailFast should fail for incomplete input data") {
        intercept[DecodeError.TypeError] {
          val r = ShowsSource
            .csvFailFast[IO, String]("""CATS,COMEDY""")
            .unsafeRunSync()
          println(r)
        }
      }
    }
  }
}
