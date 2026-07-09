# kamon-mappings-gen

kamon-mappings-gen is a modular library that automatically generates Kamon HTTP instrumentation operation mappings from framework endpoint definitions.

It keeps metrics manageable by turning dynamic URLs into stable route patterns that Kamon can group together.
For example, requests like `/books/123/reviews/456` and `/books/987/reviews/654` are both mapped to `/books/:bookId/reviews/:reviewId`.

## Project Modules

**Tapir (Scala)**

*   **kamon-config**: Core library for generating Kamon mappings from Tapir endpoints.
*   **kamon-config-cli**: CLI for build-time generation of Tapir-based Kamon mappings.

**http4k (Kotlin)**

*   **kamon-config-http4k**: Core library for generating Kamon mappings from http4k endpoints.
*   **kamon-config-http4k-cli**: CLI for build-time generation of http4k-based Kamon mappings.

## Usage

There are two approaches to integrate endpoint mappings into your application.

Before using any module, add [JitPack](https://jitpack.io/) to your repositories.

### Option A: Dynamic Runtime Loading (Recommended)

This approach generates and merges telemetry mappings directly during application startup.

#### Tapir

##### Dependency:

```
com.github.vaslabs-ltd.kamon-mappings-gen:tapir-kamon-config_3:VERSION
```

###### Initialize Kamon in your application startup code:

Segment of the `tapir-lib` example in [`examples/tapir/tapir-lib`](examples/tapir/tapir-lib):

```scala
import io.vaslabs.kamon.tapir.http4s.Http4sFormatter
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

### Option B: Build-Time Generation

This approach runs the generator at compile time and packages the output `reference.conf` file directly in your JAR resources.

#### Tapir

##### Dependency:

```
com.github.vaslabs-ltd.kamon-mappings-gen:tapir-kamon-config-cli_3:VERSION
```


**Mill integration:**

Segment of the `tapir-cli` object in [`examples/tapir/package.mill`](examples/tapir/package.mill) example:
```scala
def generateKamonConfig = Task {
  val dest = Task.dest / "reference.conf"
  val cp = Seq(compile().classes.path) ++ compileClasspath().map(_.path)
  mill.util.Jvm.callProcess(
    mainClass = "io.vaslabs.kamon.tapir.cli.ConfigGeneratorCli",
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
