package io.vaslabs.kamon.tapir.http4k

import kamon.Kamon
import kamon.trace.Span
import org.http4k.contract.ContractRoute
import org.http4k.contract.Root
import org.http4k.core.Filter

/**
 * Decomposed path template into literal and param segments
 */
data class RouteTemplate(val template: String, val segments: List<Segment>) {

    sealed class Segment {
        data class Literal(val value: String) : Segment()
        data class Param(val name: String) : Segment()
    }

    fun matches(path: String): Boolean {
        val pathSegments = path.trim('/').split("/")
        if (pathSegments.size != segments.size) return false
        return segments.zip(pathSegments).all { (segment, actual) ->
            when (segment) {
                is Segment.Literal -> segment.value == actual
                is Segment.Param -> true
            }
        }
    }

    companion object {
        private val PATH_PARAM_REGEX = Regex("\\{([^}]+)\\}")

        fun from(template: String): RouteTemplate {
            val segments = template.trim('/').split("/").map { part ->
                val match = PATH_PARAM_REGEX.matchEntire(part)
                if (match != null) Segment.Param(match.groupValues[1]) else Segment.Literal(part)
            }
            return RouteTemplate(template, segments)
        }
    }
}

/**
 * Extracts RouteTemplates from a set of http4k contract routes.
 */
object RouteTemplates {
    fun extractRouteTemplates(routes: List<ContractRoute>): List<RouteTemplate> =
        routes.map { route -> RouteTemplate.from(route.describeFor(Root)) }
}

/**
 * Resolves a raw request path into its route template at runtime,
 * since a Kamon's automatic instrumentation is unavailable for http4k.
 */
class RouteTemplateResolver(routes: List<ContractRoute>) {

    private val templates: List<RouteTemplate> =
        RouteTemplates.extractRouteTemplates(routes)
            .sortedByDescending { it.segments.count { seg -> seg is RouteTemplate.Segment.Literal } }

    fun resolve(path: String): String =
        templates.firstOrNull { it.matches(path) }?.template ?: "unmatched"
}

/**
 * Kamon tracing filter setup. Resolves routes' templates instead of raw path
 *
 * Usage: KamonTracing.filter(allRoutes).then(app)
 */
object KamonTracing {
    fun filter(routes: List<ContractRoute>): Filter {
        val resolver = RouteTemplateResolver(routes)

        return Filter { next ->
            { request ->
                val span = Kamon.spanBuilder(resolver.resolve(request.uri.path))
                    .tag("http.method", request.method.name)
                    .start()

                try {
                    Kamon.runWithContext(Kamon.currentContext().withEntry(Span.Key(), span)) {
                        next(request).apply {
                            span.tag("http.status_code", status.code.toString())
                        }
                    }
                } finally {
                    span.finish()
                }
            }
        }
    }
}