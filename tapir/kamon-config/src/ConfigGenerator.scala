package org.vaslabs.kamon.mappings.tapir

import sttp.tapir.*
import sttp.tapir.internal.RichEndpointInput

object ConfigGenerator:

  /** Extracts raw mapping pairs (Match Pattern, Template Pattern) from endpoints. E.g., ("/books/wildcard", "/books/:bookId")
    * @param endpoints
    *   Α list of Tapir endpoints to extract mappings from.
    * @return
    *   A sequence of tuples containing the match pattern and template pattern for each endpoint with path variables.
    */
  def extractMappings(endpoints: List[AnyEndpoint]): Seq[(String, String)] =
    endpoints.flatMap { endpoint =>
      val basicInputs = endpoint.input.asVectorOfBasicInputs(true)

      val pathInputs = basicInputs.collect {
        case p: EndpointInput.FixedPath[?]   => p
        case c: EndpointInput.PathCapture[?] => c
      }

      val hasCaptures = pathInputs.exists(_.isInstanceOf[EndpointInput.PathCapture[?]])

      if (hasCaptures) {
        val key = pathInputs
          .map {
            case p: EndpointInput.FixedPath[?]   => p.s
            case _: EndpointInput.PathCapture[?] => "*"
            case _                               => "" // suppress compiler warning
          }
          .mkString("/", "/", "")

        val value = pathInputs
          .map {
            case p: EndpointInput.FixedPath[?]   => p.s
            case c: EndpointInput.PathCapture[?] => s":${c.name.getOrElse("param")}"
            case _                               => "" // suppress compiler warning
          }
          .mkString("/", "/", "")

        Some((key, value))
      } else {
        None
      }
    }

  /** Reusable formatting template that generates the Kamon HOCON mappings structure for a given backend instrumentation key.
    * @param mappings
    *   A sequence of tuples containing the match pattern and template pattern for each endpoint with path variables.
    * @param backendKey
    *   The Kamon backend instrumentation key (e.g., "http4s", "akka-http") to be used in the HOCON structure.
    * @return
    *   A string containing the formatted HOCON configuration for the specified backend instrumentation.
    */
  def formatHocon(mappings: Seq[(String, String)], backendKey: String): String =
    val indentSize = 10
    val mappingsBlock = mappings.distinct
      .map { case (key, value) =>
        s"""${" " * indentSize}"$key" = "$value""""
      }
      .mkString("\n")

    s"""kamon.instrumentation.$backendKey {
       |  server {
       |    tracing {
       |      operations {
       |        mappings {
       |$mappingsBlock
       |        }
       |      }
       |    }
       |  }
       |}""".stripMargin
