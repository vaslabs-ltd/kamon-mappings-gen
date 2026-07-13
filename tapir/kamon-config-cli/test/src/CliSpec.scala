package org.vaslabs.kamon.mappings.tapir.cli

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File
import java.nio.file.Files
import sttp.tapir.*

object TestEndpoints {
  val all: Seq[AnyEndpoint] = Seq(
    endpoint.get.in("books" / path[String]("bookId"))
  )
}

object TestEndpoints2 {
  val all: Seq[AnyEndpoint] = Seq(
    endpoint.get.in("authors" / path[String]("authorId"))
  )
}

class CliSpec extends AnyFlatSpec with Matchers {

  "Tapir Cli" should "generate kamon config for a single class with http4s backend" in {
    val tempFile = File.createTempFile("kamon-config-tapir-", ".conf")
    tempFile.deleteOnExit()

    ConfigGeneratorCli.main(
      Array(
        "--endpoints-path",
        "org.vaslabs.kamon.mappings.tapir.cli.TestEndpoints.all",
        "--output-path",
        tempFile.getAbsolutePath,
        "--backend",
        "http4s"
      )
    )

    val hocon = Files.readString(tempFile.toPath)
    val expected = """kamon.instrumentation.http4s {
                     |  server {
                     |    tracing {
                     |      operations {
                     |        mappings {
                     |          "/books/*" = "/books/:bookId"
                     |        }
                     |      }
                     |    }
                     |  }
                     |}""".stripMargin

    hocon shouldBe expected
  }

  it should "generate kamon config for multiple aggregated classes with akka-http backend" in {
    val tempFile = File.createTempFile("kamon-config-tapir-", ".conf")
    tempFile.deleteOnExit()

    ConfigGeneratorCli.main(
      Array(
        "--endpoints-path",
        "org.vaslabs.kamon.mappings.tapir.cli.TestEndpoints.all",
        "--endpoints-path",
        "org.vaslabs.kamon.mappings.tapir.cli.TestEndpoints2.all",
        "--output-path",
        tempFile.getAbsolutePath,
        "--backend",
        "akka-http"
      )
    )

    val hocon = Files.readString(tempFile.toPath)
    val expected = """kamon.instrumentation.akka.http {
                     |  server {
                     |    tracing {
                     |      operations {
                     |        mappings {
                     |          "/books/*" = "/books/:bookId"
                     |          "/authors/*" = "/authors/:authorId"
                     |        }
                     |      }
                     |    }
                     |  }
                     |}""".stripMargin

    hocon shouldBe expected
  }
}
