package com.softwaremill

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, Port, port}
import io.vaslabs.kamon.mappings.tapir.http4s.Http4sFormatter
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] = {
    val config = Http4sFormatter.loadConfig(Endpoints.all)
    IO.delay(kamon.Kamon.init(config)) >> {
      val routes = Http4sServerInterpreter[IO]().toRoutes(Endpoints.all)
      val port = sys.env
        .get("HTTP_PORT")
        .flatMap(_.toIntOption)
        .flatMap(Port.fromInt)
        .getOrElse(port"8080")
      val tracedRoutes = kamon.http4s.middleware.server.KamonSupport(routes, "localhost", port.value)

      EmberServerBuilder
        .default[IO]
        .withHost(Host.fromString("localhost").get)
        .withPort(port)
        .withHttpApp(Router("/" -> tracedRoutes).orNotFound)
        .build
        .use: server =>
          for
            _ <- IO.println(s"Go to http://localhost:${server.address.getPort}/docs to open SwaggerUI. Press ENTER key to exit.")
            _ <- IO.readLine
          yield ()
        .as(ExitCode.Success)
    }
  }
