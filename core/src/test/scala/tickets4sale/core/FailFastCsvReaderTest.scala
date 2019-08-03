package tickets4sale.core

import java.io.File
import java.time.LocalDate

import Domain._
import kantan.csv.DecodeError
import tickets4sale.core.StaticShowRepository._
import utest._

object FailFastCsvReaderTest extends TestSuite {
  val tests = Tests {
    test("FailFastCsvReader") {
      val reader = new FailFastCsvReader

      test("read should reliably extract data with known CSV format even with empty lines") {
        val expect = Right(List(
            Show("1984", LocalDate.parse("2019-10-14"), Genre.Drama)
          , Show("39 steps, the ", LocalDate.parse("2019-11-10"), Genre.Comedy)
          , Show("a midsummer night’s dream - in new orleans", LocalDate.parse("2020-04-28"), Genre.Drama)
        ))
        val actual = reader.read[File](resourceFile("example.csv"))

        assert(expect == actual)
      }

      test("read should fail for unexpected input data") {
        intercept[DecodeError.TypeError] {
          reader.read[String]("""CATS,2019-08-15,TEST""").toTry.get
        }
      }

      test("read should fail for incomplete input data") {
        intercept[DecodeError.TypeError] {
          reader.read[String]("""CATS,COMEDY""").toTry.get
        }
      }
    }
  }
}
