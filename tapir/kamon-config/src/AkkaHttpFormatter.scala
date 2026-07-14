package org.vaslabs.kamon.mappings.tapir.akka

import org.vaslabs.kamon.mappings.tapir.BaseFormatter

object AkkaHttpFormatter extends BaseFormatter {
  override val backend: String = "akka.http"
}
