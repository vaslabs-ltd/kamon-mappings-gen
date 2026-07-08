package io.vaslabs.kamon.tapir.akka

import io.vaslabs.kamon.tapir.BaseFormatter

object AkkaHttpFormatter extends BaseFormatter:
  override val backend: String = "akka.http"
