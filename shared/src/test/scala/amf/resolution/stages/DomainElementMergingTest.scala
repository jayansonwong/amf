package amf.resolution.stages

import amf.domain.`abstract`.ParametrizedTrait
import amf.plugins.domain.shapes.models.ScalarShape
import amf.plugins.domain.webapi.models.EndPoint
import amf.resolution.stages.DomainElementMerging.merge
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * Created by pedro.colunga on 10/31/17.
  */
class DomainElementMergingTest extends FunSuite {

  test("Merge EndPoints") {

    val main = EndPoint()
      .withName("Main")
      .withPath("/main")
      .withDescription("Main description")

    main
      .withOperation("get")
      .withSummary("Get main operation")
      .withRequest()
      .withQueryParameter("a")
      .withDescription("Main query parameter a")

    main.withOperation("post").withSummary("Post main operation")
    main.withOperation("head")

    val other = EndPoint()
      .withName("Other")
      .withPath("/other")
      .withDescription("Other description")

    other
      .withOperation("get")
      .withSummary("Get other operation")
      .withRequest()
      .withQueryParameter("a")
      .withDescription("Other query parameter a")
      .withScalarSchema("integer")
      .withDataType("integer")

    other.withOperation("put").withSummary("Put other operation")
    other.withOperation("head").withSummary("Head other operation")

    merge(main, other)

    main.operations.size should be(4)

    val get = main.operations.head
    get.summary should be("Get main operation")
    get.method should be("get")

    val parameters = get.request.queryParameters
    parameters.size should be(1)
    val a = parameters.head
    a.name should be("a")
    a.description should be("Main query parameter a")
    a.schema.asInstanceOf[ScalarShape].dataType should be("integer")

    val post = main.operations(1)
    post.summary should be("Post main operation")
    post.method should be("post")

    val head = main.operations(2)
    head.summary should be("Head other operation")
    head.method should be("head")

    val put = main.operations(3)
    put.summary should be("Put other operation")
    put.method should be("put")
  }

  test("Do not merge extends") {

    val a = ParametrizedTrait()
      .withName("a")
      .withTarget("/trait/a")

    val b = ParametrizedTrait()
      .withName("b")
      .withTarget("/trait/b")

    val main = EndPoint()
      .withName("Main")
      .withPath("/main")
      .withExtends(Seq(a))

    val other = EndPoint()
      .withName("Other")
      .withPath("/other")
      .withExtends(Seq(b))

    merge(main, other)

    main.extend.size should be(1)
    main.extend.head should be(a)
  }
}
