package io.vaslabs.kamon.tapir.http4k

import org.http4k.contract.bindContract
import org.http4k.contract.div
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RouteTemplateSpec {

    @Test
    fun `parses a literal-only template`() {
        val template = RouteTemplate.from("/ping")

        assertEquals("/ping", template.template)
        assertEquals(listOf(RouteTemplate.Segment.Literal("ping")), template.segments)
    }

    @Test
    fun `parses a template with a single param`() {
        val template = RouteTemplate.from("/books/{bookId}")

        assertEquals(
            listOf(RouteTemplate.Segment.Literal("books"), RouteTemplate.Segment.Param("bookId")),
            template.segments
        )
    }

    @Test
    fun `parses a template with multiple params`() {
        val template = RouteTemplate.from("/books/{bookId}/reviews/{reviewId}")

        assertEquals(
            listOf(
                RouteTemplate.Segment.Literal("books"),
                RouteTemplate.Segment.Param("bookId"),
                RouteTemplate.Segment.Literal("reviews"),
                RouteTemplate.Segment.Param("reviewId")
            ),
            template.segments
        )
    }

    @Test
    fun `matches a path with the same shape`() {
        val template = RouteTemplate.from("/books/{bookId}")

        assertTrue(template.matches("/books/abc"))
        assertTrue(template.matches("/books/123"))
    }

    @Test
    fun `does not match a path with a different number of segments`() {
        val template = RouteTemplate.from("/books/{bookId}")

        assertFalse(template.matches("/books"))
        assertFalse(template.matches("/books/abc/reviews"))
    }

    @Test
    fun `matches a path with multiple params in the correct positions`() {
        val template = RouteTemplate.from("/books/{bookId}/reviews/{reviewId}")

        assertTrue(template.matches("/books/abc/reviews/xyz"))
        assertFalse(template.matches("/books/abc/comments/xyz"))
    }
}

class RouteTemplatesSpec {

    @Test
    fun `extracts a template from a route with a single param`() {
        val route = "/books" / Path.of("bookId") bindContract Method.GET to { _: String -> { Response(Status.OK) } }

        val templates = RouteTemplates.extractRouteTemplates(listOf(route))

        assertEquals(1, templates.size)
        assertEquals("/books/{bookId}", templates[0].template)
    }

    @Test
    fun `extracts a template from a route with multiple params and a literal segment between them`() {
        val route = "/books" / Path.of("bookId") / "reviews" / Path.of("reviewId") bindContract
                Method.GET to { bookId: String, reviews: String, reviewId: String -> { Response(Status.OK) } }

        val templates = RouteTemplates.extractRouteTemplates(listOf(route))

        assertEquals(1, templates.size)
        assertEquals("/books/{bookId}/reviews/{reviewId}", templates[0].template)
    }

    @Test
    fun `extracts templates from multiple routes`() {
        val pingRoute = "/ping" bindContract Method.GET to { _ -> Response(Status.OK) }
        val bookRoute = "/books" / Path.of("bookId") bindContract Method.GET to { _: String -> { Response(Status.OK) } }

        val templates = RouteTemplates.extractRouteTemplates(listOf(pingRoute, bookRoute))

        assertEquals(setOf("/ping", "/books/{bookId}"), templates.map { it.template }.toSet())
    }
}

class RouteTemplateResolverSpec {

    @Test
    fun `resolves a path to its matching template`() {
        val route = "/books" / Path.of("bookId") bindContract Method.GET to { _: String -> { Response(Status.OK) } }
        val resolver = RouteTemplateResolver(listOf(route))

        assertEquals("/books/{bookId}", resolver.resolve("/books/abc"))
    }

    @Test
    fun `resolves an unmatched path to the unmatched marker`() {
        val route = "/books" / Path.of("bookId") bindContract Method.GET to { _: String -> { Response(Status.OK) } }
        val resolver = RouteTemplateResolver(listOf(route))

        assertEquals("unmatched", resolver.resolve("/authors/abc"))
    }

    @Test
    fun `prefers the more specific template when multiple could match`() {
        val genericRoute = "/books" / Path.of("bookId") bindContract Method.GET to { _: String -> { Response(Status.OK) } }
        val specificRoute = "/books" / Path.of("bookId") / "reviews" bindContract
                Method.GET to { bookId: String, reviews: String -> { Response(Status.OK) } }

        val resolver = RouteTemplateResolver(listOf(genericRoute, specificRoute))

        assertEquals("/books/{bookId}/reviews", resolver.resolve("/books/abc/reviews"))
        assertEquals("/books/{bookId}", resolver.resolve("/books/abc"))
    }
}