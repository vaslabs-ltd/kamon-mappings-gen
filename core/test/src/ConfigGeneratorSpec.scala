package io.vaslabs.kamon.tapir

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.tapir.*

class ConfigGeneratorSpec extends AnyFlatSpec with Matchers:

  it should "extract empty mappings when no endpoints have path variables" in {
    val ep = endpoint.get.in("hello" / "world")
    ConfigGenerator.extractMappings(List(ep)) shouldBe empty
  }

  it should "extract mapping for a single path variable" in {
    val ep = endpoint.get.in("books" / path[String]("bookId"))
    ConfigGenerator.extractMappings(List(ep)) should contain theSameElementsAs Seq(
      "/books/*" -> "/books/:bookId"
    )
  }

  it should "extract mapping for multiple path variables" in {
    val ep = endpoint.get.in("users" / path[String]("userId") / "posts" / path[String]("postId"))
    ConfigGenerator.extractMappings(List(ep)) should contain theSameElementsAs Seq(
      "/users/*/posts/*" -> "/users/:userId/posts/:postId"
    )
  }
