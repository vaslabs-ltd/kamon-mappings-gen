package io.vaslabs.kamon.tapir.http4s

import io.vaslabs.kamon.tapir.ConfigGenerator
import com.typesafe.config.{Config, ConfigFactory}
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

object Http4sFormatter:

  def format(mappings: Seq[(String, String)]): String =
    ConfigGenerator.formatHocon(mappings, "http4s")

  /** Generates a Typesafe Config containing the Kamon http4s tracing mappings merged with default classpath configuration.
    * @param endpoints
    *   A list of Tapir endpoints to extract mappings from.
    * @return
    *   The generated Typesafe Config containing the Kamon http4s tracing mappings merged with default classpath configuration.
    */
  def loadConfig(endpoints: List[AnyEndpoint]): Config =
    val rawMappings = ConfigGenerator.extractMappings(endpoints)
    val hoconString = format(rawMappings)
    ConfigFactory.parseString(hoconString).withFallback(ConfigFactory.load())

  /** Overload that accepts a list of ServerEndpoints directly.
    */
  def loadConfig(endpoints: List[ServerEndpoint[?, ?]])(implicit d: DummyImplicit): Config =
    loadConfig(endpoints.map(_.endpoint))
