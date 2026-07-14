package org.vaslabs.kamon.mappings.tapir.http4s

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Http4sFormatterSpec extends AnyFlatSpec with Matchers {

  it should "format mappings into http4s HOCON wrapper" in {
    val mappings = Seq("/books/*" -> "/books/:bookId")
    val hocon = Http4sFormatter.format(mappings)
    hocon should include("kamon.instrumentation.http4s")
    hocon should include(s""""/books/*" = "/books/:bookId"""")
  }
}
