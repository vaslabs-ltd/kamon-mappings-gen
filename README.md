# kamon-mappings-gen

[![Maven Central](https://img.shields.io/maven-central/v/org.vaslabs/kamon-mappings-tapir_3.svg)](https://central.sonatype.com/artifact/org.vaslabs/kamon-mappings-tapir_3)

kamon-mappings-gen is a modular library that automatically generates Kamon HTTP instrumentation operation mappings from framework endpoint definitions.

It keeps metrics manageable by turning dynamic URLs into stable route patterns that Kamon can group together.
For example, requests like `/books/123/reviews/456` and `/books/987/reviews/654` are both mapped to `/books/:bookId/reviews/:reviewId`.

## Project Modules

**Tapir (Scala)**

*   **kamon-mappings-tapir**: Core library for generating Kamon mappings from Tapir endpoints (supports Scala 2.13 and Scala 3).
*   **kamon-mappings-tapir-cli**: CLI for build-time generation of Tapir-based Kamon mappings (supports Scala 2.13 and Scala 3).

**http4k (Kotlin)**

*   **kamon-mappings-http4k**: Core library for generating Kamon mappings from http4k endpoints.
*   **kamon-mappings-http4k-cli**: CLI for build-time generation of http4k-based Kamon mappings.

---

## Usage

There are two approaches to integrate endpoint mappings into your application.

### Option A: Dynamic Runtime Loading (Recommended)

This approach generates and merges telemetry mappings directly during application startup.

#### Tapir (Scala)

##### Dependency:

```
org.vaslabs:kamon-mappings-tapir_3:xxx      // For Scala 3
org.vaslabs:kamon-mappings-tapir_2.13:xxx   // For Scala 2.13
```

##### Initialize Kamon in your application startup code:

Segment of the `tapir-lib` example in [`examples/tapir/tapir-lib`](examples/tapir/tapir-lib):

```scala
import org.vaslabs.kamon.mappings.tapir.http4s.Http4sFormatter
import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    IO.delay {
      val config = Http4sFormatter.loadConfig(Endpoints.all)
      kamon.Kamon.init(config)
    } >> {
      // Start your server ...
    }
```

#### http4k

--

---

### Option B: Build-Time Generation

This approach runs the generator at compile time and packages the output `reference.conf` file directly in your JAR resources.

#### Tapir (Scala)

##### Dependency:

```
org.vaslabs:kamon-mappings-tapir-cli_3:xxx      // For Scala 3
org.vaslabs:kamon-mappings-tapir-cli_2.13:xxx   // For Scala 2.13
```

**Mill integration:**

Segment of the `tapir-cli` object in [`examples/tapir/package.mill`](examples/tapir/package.mill) example:
```scala
def generateKamonConfig = Task {
  val dest = Task.dest / "reference.conf"
  val cp = Seq(compile().classes.path) ++ compileClasspath().map(_.path)
  mill.util.Jvm.callProcess(
    mainClass = "org.vaslabs.kamon.mappings.tapir.cli.ConfigGeneratorCli",
    classPath = cp,
    mainArgs = Seq(
      "--endpoints-path", "com.mycompany.Endpoints.all", 
      "--output-path", dest.toString
    ),
    cwd = Task.dest
  )
  PathRef(Task.dest)
}

override def resources = Task {
  super.resources() ++ Seq(generateKamonConfig())
}
```

#### http4k

--

---

## Development

This project is built using Mill.

Compile all modules:
```bash
./mill __.compile
```

Run all tests:
```bash
./mill __.test
```

Reformat code:
```bash
./mill mill.scalalib.scalafmt/ 
```
