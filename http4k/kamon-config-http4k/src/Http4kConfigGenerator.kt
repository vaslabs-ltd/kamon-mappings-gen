package io.vaslabs.kamon.mappings.http4k

import org.http4k.contract.ContractRoute
import org.http4k.contract.Root

object Http4kConfigGenerator {
    private val PATH_PARAM_REGEX = Regex("\\{([^}]+)\\}")

    fun extractMappings(routes: List<ContractRoute>): List<Pair<String, String>> =
        routes.map { route ->
            val template = route.describeFor(Root)
            val matchPattern = template.replace(PATH_PARAM_REGEX, "*")
            val templatePattern = template.replace(PATH_PARAM_REGEX, ":$1")
            matchPattern to templatePattern
        }
}
