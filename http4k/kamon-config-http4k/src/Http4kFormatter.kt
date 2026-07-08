package io.vaslabs.kamon.tapir.http4k

import org.http4k.contract.ContractRoute

object Http4kFormatter {
    fun format(routes: List<ContractRoute>, backend: String = "http"): String {
        val indent = 10
        val mappings = Http4kConfigGenerator.extractMappings(routes)
        val indentPrefix = " ".repeat(indent)
        val mappingLines = mappings.joinToString("\n") { (matching, template) ->
            "$indentPrefix\"$matching\" = \"$template\""
        }
        return """
            |kamon.instrumentation.$backend {
            |  server {
            |    tracing {
            |      operations {
            |        mappings {
            |$mappingLines
            |        }
            |      }
            |    }
            |  }
            |}
        """.trimMargin()
    }
}
