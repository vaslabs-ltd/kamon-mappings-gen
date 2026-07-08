package io.vaslabs.kamon.tapir.http4s

import io.vaslabs.kamon.tapir.BaseFormatter

object Http4sFormatter extends BaseFormatter:
  override val backend: String = "http4s"
