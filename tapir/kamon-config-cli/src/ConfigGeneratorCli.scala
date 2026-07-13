package io.vaslabs.kamon.mappings.tapir.cli

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets
import mainargs.{main, arg, ParserForMethods}
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint
import io.vaslabs.kamon.mappings.tapir.ConfigGenerator
import io.vaslabs.kamon.mappings.tapir.http4s.Http4sFormatter
import io.vaslabs.kamon.mappings.tapir.akka.AkkaHttpFormatter

object ConfigGeneratorCli:
  @main
  def run(
      @arg(name = "endpoints-path", doc = "Fully qualified name(s) of the endpoints list, e.g. com.softwaremill.Endpoints.all")
      endpointsPaths: Seq[String],
      @arg(name = "output-path", doc = "Output file path for the HOCON configuration")
      outputPath: String,
      @arg(name = "backend", doc = "Target Kamon instrumentation backend (e.g. http4s, akka-http)")
      backend: String = "http4s"
  ): Unit =
    val path = Paths.get(outputPath)

    try {
      val endpoints = endpointsPaths.flatMap { endpointsPath =>
        val lastDot = endpointsPath.lastIndexOf('.')
        if (lastDot == -1) {
          throw new IllegalArgumentException(
            s"Endpoints path must be fully qualified with an object and field name, e.g. com.softwaremill.Endpoints.all. Found: $endpointsPath"
          )
        }

        val objectClassName = endpointsPath.substring(0, lastDot)
        val memberName = endpointsPath.substring(lastDot + 1)

        val instance = try {
          val clazz = Class.forName(s"${objectClassName}$$")
          clazz.getField("MODULE$").get(null)
        } catch {
          case _: ClassNotFoundException | _: NoSuchFieldException =>
            Class.forName(objectClassName).getDeclaredConstructor().newInstance()
        }

        val rawSeq = instance.getClass.getMethod(memberName).invoke(instance) match {
          case seq: Seq[?] => seq
          case _           => throw new IllegalArgumentException(s"$endpointsPath did not return a Seq of endpoints.")
        }

        rawSeq.flatMap {
          case se: ServerEndpoint[?, ?] => Some(se.endpoint)
          case e: AnyEndpoint           => Some(e)
          case other                    =>
            System.err.println(s"Warning: Ignored unknown element in list: $other")
            None
        }
      }.toList

      // Generate HOCON and write file
      val rawMappings = ConfigGenerator.extractMappings(endpoints)
      val hoconContent = backend.toLowerCase match {
        case "http4s"    => Http4sFormatter.format(rawMappings)
        case "akka-http" => AkkaHttpFormatter.format(rawMappings)
        case other       => throw new IllegalArgumentException(s"Unsupported backend: $other")
      }

      Option(path.getParent).foreach(Files.createDirectories(_))
      Files.write(path, hoconContent.getBytes(StandardCharsets.UTF_8))
      println(s"Successfully generated Kamon configuration mappings for ${endpointsPaths.mkString(", ")} at $path")

    } catch {
      case e: Exception =>
        e.printStackTrace()
        System.exit(1)
    }

  def main(args: Array[String]): Unit =
    ParserForMethods(this).runOrExit(args.toIndexedSeq)
