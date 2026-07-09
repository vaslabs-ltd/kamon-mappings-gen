package com.softwaremill

import com.softwaremill.Endpoints.{*, given}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client4.{UriContext, basicRequest}

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.generic.auto.*
import sttp.client4.circe.*
import sttp.client4.testing.BackendStub
import sttp.tapir.integ.cats.effect.CatsMonadError
import sttp.tapir.server.stub4.TapirStubInterpreter
import Library.*
import sttp.tapir.internal.RichEndpointInput

class EndpointsSpec extends AnyFlatSpec with Matchers with EitherValues:

  it should "return hello message" in {
    // given
    val backendStub = TapirStubInterpreter(BackendStub[IO](new CatsMonadError[IO]()))
      .whenServerEndpointRunLogic(helloServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/hello?name=adam")
      .send(backendStub)

    // then
    response.map(_.body.value shouldBe "Hello adam").unwrap
  }

  it should "list available books" in {
    // given
    val backendStub = TapirStubInterpreter(BackendStub[IO](new CatsMonadError[IO]()))
      .whenServerEndpointRunLogic(booksListingServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/books/list/all")
      .response(asJson[List[Book]])
      .send(backendStub)

    // then
    response.map(_.body.value shouldBe books).unwrap
  }

  extension [T](t: IO[T]) def unwrap: T = t.unsafeRunSync()
