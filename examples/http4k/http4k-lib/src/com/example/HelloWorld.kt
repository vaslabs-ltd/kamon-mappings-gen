package com.example

import com.example.formats.JacksonMessage
import com.example.formats.jacksonMessageLens
import org.http4k.contract.bindContract
import org.http4k.contract.contract
import org.http4k.contract.div
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.lens.Path
import org.http4k.server.Netty
import org.http4k.server.asServer
import kamon.Kamon
import kamon.http4k.KamonFilter

val pingRoute = "/ping" bindContract GET to { _: Request -> Response(OK).body("pong") }

val jacksonRoute = "/formats/json/jackson" bindContract GET to { _: Request ->
    Response(OK).with(jacksonMessageLens of JacksonMessage("Barry", "Hello there!"))
}

val greetRoute = "/greet" / Path.of("name") bindContract GET to { name ->
    { _: Request -> Response(OK).body("Hello $name!") }
}

val allRoutes = listOf(pingRoute, jacksonRoute, greetRoute)

val app: HttpHandler = contract {
    routes += allRoutes
}

fun main() {
    Kamon.init()

     val printingApp: HttpHandler = PrintRequest()
        .then(KamonFilter.apply("0.0.0.0", 9000))
        .then(app)

    val server = printingApp.asServer(Netty(9000)).start()

    println("Server started on " + server.port())
}
