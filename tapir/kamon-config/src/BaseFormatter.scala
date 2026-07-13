package io.vaslabs.kamon.mappings.tapir

import com.typesafe.config.{Config, ConfigFactory}
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

trait BaseFormatter:
  def backend: String

  def format(mappings: Seq[(String, String)]): String =
    ConfigGenerator.formatHocon(mappings, backend)

  def loadConfig(endpoints: List[AnyEndpoint]): Config =
    val rawMappings = ConfigGenerator.extractMappings(endpoints)
    val hoconString = format(rawMappings)
    ConfigFactory.parseString(hoconString).withFallback(ConfigFactory.load())

  def loadConfig(endpoints: List[ServerEndpoint[?, ?]])(implicit d: DummyImplicit): Config =
    loadConfig(endpoints.map(_.endpoint))
