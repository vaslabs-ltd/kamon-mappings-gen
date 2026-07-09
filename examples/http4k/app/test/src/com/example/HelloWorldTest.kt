package com.example

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import io.vaslabs.kamon.tapir.http4k.Http4kFormatter

class HelloWorldTest {

    @Test
    fun `Ping test`() {
        assertEquals(Response(OK).body("pong"), app(Request(GET, "/ping")))
    }

    @Test
    fun `Kamon config mapping test`() {
        val config = Http4kFormatter.loadConfig(allRoutes, "netty")
        assertTrue(config.hasPath("kamon.instrumentation.netty.server.tracing.operations.mappings"))

        val mappings = config.getObject("kamon.instrumentation.netty.server.tracing.operations.mappings").toConfig()
        assertTrue(mappings.hasPath("\"/greet/*\""))
        assertEquals("/greet/:name", mappings.getString("\"/greet/*\""))
    }
}
