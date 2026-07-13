package io.vaslabs.kamon.mappings.http4k

import org.http4k.contract.Root
import org.http4k.contract.bindContract
import org.http4k.contract.div
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.http4k.lens.string
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Http4kConfigGeneratorSpec {
    @Test
    fun testRouteExtraction() {
        val route = "/books" / Path.of("bookId") bindContract Method.GET to { _ -> { Response(Status.OK) } }

        val mappings = Http4kConfigGenerator.extractMappings(listOf(route))
        assertEquals(1, mappings.size)
        assertEquals("/books/*" to "/books/:bookId", mappings[0])
    }

    @Test
    fun testRouteExtractionWithMultipleParams() {
        val route = "/books" / Path.of("bookId") / "reviews" / Path.of("reviewId") bindContract Method.GET to { bookId: String, reviews: String, reviewId: String -> { Response(Status.OK) } }
        
        val mappings = Http4kConfigGenerator.extractMappings(listOf(route))
        assertEquals(1, mappings.size)
        assertEquals("/books/*/reviews/*" to "/books/:bookId/reviews/:reviewId", mappings[0])
    }
}
