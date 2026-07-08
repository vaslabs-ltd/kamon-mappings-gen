package io.vaslabs.kamon.tapir.http4k

import org.http4k.contract.bindContract
import org.http4k.contract.div
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Http4kFormatterSpec {
    @Test
    fun testFormatting() {
        val route = "/books" / Path.of("bookId") bindContract Method.GET to { _ -> { Response(Status.OK) } }
        val hocon = Http4kFormatter.format(listOf(route), backend = "http")
        
        assertTrue(hocon.contains("kamon.instrumentation.http"))
        val expectedMappingLine = "\"/books/*\" = \"/books/:bookId\""
        assertTrue(hocon.contains(expectedMappingLine))
    }
}
