package io.vaslabs.kamon.mappings.http4k.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import io.vaslabs.kamon.mappings.http4k.Http4kFormatter
import org.http4k.contract.ContractRoute
import java.io.File

class Http4kCli : CliktCommand(name = "kamon-config-http4k-cli") {
    override fun help(context: Context): String = "Generate Kamon HOCON instrumentation config from http4k ContractRoutes"

    val classNames by option("-c", "--class", help = "Fully qualified class/object name containing the routes").multiple()
    val fieldName by option("-f", "--field", help = "Name of the field or method containing/returning the List<ContractRoute>").default("routes")
    val backend by option("-b", "--backend", help = "The target Kamon instrumentation backend (e.g. netty, undertow, jetty)").default("http")
    val output by option("-o", "--output", help = "Path to the output file to write the config to (defaults to stdout)")
    override fun run() {
        if (classNames.isEmpty()) {
            throw UsageError("Must specify at least one class containing routes.")
        }
        val allRoutes = mutableListOf<ContractRoute>()

        for (className in classNames) {
            val clazz = Class.forName(className)
            
            // Try Kotlin object INSTANCE first, otherwise instantiate with default constructor
            val instance = try {
                clazz.getField("INSTANCE").get(null)
            } catch (e: Exception) {
                try {
                    clazz.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    null
                }
            }

            // Try getting field, then try invoking method
            val rawResult = try {
                val field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                field.get(instance)
            } catch (e: Exception) {
                val method = clazz.getDeclaredMethod(fieldName)
                method.isAccessible = true
                method.invoke(instance)
            }

            @Suppress("UNCHECKED_CAST")
            val routes = rawResult as? List<ContractRoute>
                ?: throw IllegalArgumentException("Result of '$fieldName' in '$className' is not a List<ContractRoute>")
            
            allRoutes.addAll(routes)
        }

        val hocon = Http4kFormatter.format(allRoutes, backend)

        val outputPath = output
        if (outputPath != null) {
            File(outputPath).writeText(hocon)
            echo("Successfully wrote Kamon configuration to $outputPath")
        } else {
            echo(hocon)
        }
    }
}

fun main(args: Array<String>) = Http4kCli().main(args)
