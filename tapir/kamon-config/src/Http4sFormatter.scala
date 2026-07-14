package org.vaslabs.kamon.mappings.tapir.http4s

import org.vaslabs.kamon.mappings.tapir.BaseFormatter

object Http4sFormatter extends BaseFormatter {
  override val backend: String = "http4s"
}
