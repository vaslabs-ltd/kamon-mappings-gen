package org.vaslabs.kamon.mappings.tapir.akka

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AkkaHttpFormatterSpec extends AnyFlatSpec with Matchers:

  it should "format mappings into akka-http HOCON wrapper" in {
    val mappings = Seq("/books/*" -> "/books/:bookId")
    val hocon = AkkaHttpFormatter.format(mappings)
    hocon should include("kamon.instrumentation.akka.http")
    hocon should include(s""""/books/*" = "/books/:bookId"""")
  }
