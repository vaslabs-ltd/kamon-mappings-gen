package org.vaslabs.kamon.mappings.http4k.cli

import com.github.ajalt.clikt.core.main
import org.http4k.contract.bindContract
import org.http4k.contract.div
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class Http4kCliSpec {
    object RoutesObject {
        @JvmStatic
        val routes = listOf(
            "/books" / Path.of("bookId") bindContract Method.GET to { _ -> { Response(Status.OK) } }
        )
    }

    object RoutesObject2 {
        @JvmStatic
        val routes = listOf(
            "/authors" / Path.of("authorId") bindContract Method.GET to { _ -> { Response(Status.OK) } }
        )
    }

    @Test
    fun testCliExecution() {
        val tempFile = File.createTempFile("kamon-config-", ".conf")
        tempFile.deleteOnExit()

        Http4kCli().main(arrayOf(
            "--class", "org.vaslabs.kamon.mappings.http4k.cli.Http4kCliSpec\$RoutesObject",
            "--field", "routes",
            "--output", tempFile.absolutePath
        ))

        val hocon = tempFile.readText()
        val expected = """
            |kamon.instrumentation.http4k {
            |  server {
            |    tracing {
            |      operations {
            |        mappings {
            |          "/books/*" = "/books/:bookId"
            |        }
            |      }
            |    }
            |  }
            |}
        """.trimMargin()

        assertEquals(expected, hocon)
    }

    @Test
    fun testCliExecutionWithMultipleClasses() {
        val tempFile = File.createTempFile("kamon-config-", ".conf")
        tempFile.deleteOnExit()

        Http4kCli().main(arrayOf(
            "--class", "org.vaslabs.kamon.mappings.http4k.cli.Http4kCliSpec\$RoutesObject",
            "--class", "org.vaslabs.kamon.mappings.http4k.cli.Http4kCliSpec\$RoutesObject2",
            "--field", "routes",
            "--output", tempFile.absolutePath
        ))

        val hocon = tempFile.readText()
        val expected = """
            |kamon.instrumentation.http4k {
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
            |}
        """.trimMargin()

        assertEquals(expected, hocon)
    }
}
