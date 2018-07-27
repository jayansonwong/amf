package amf.plugins.document.webapi.validation

import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.{DataNode, ScalarNode, Shape}
import amf.core.parser.Annotations
import amf.core.utils._
import amf.core.validation.ValidationCandidate
import amf.core.vocabulary.Namespace
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ExampleModel, ScalarShapeModel}
import amf.plugins.domain.shapes.models.{AnyShape, Example, ScalarShape}

import scala.collection.mutable

class PayloadsInApiCollector(model: BaseUnit) {

  val idCounter = new IdCounter()

  def collect(): Seq[ValidationCandidate] = {
    // we find all examples with strict validation
    findCandidates()
  }

  private def anyShapeRestrictions =
    Seq(AnyShapeModel.Values,
        AnyShapeModel.Inherits,
        AnyShapeModel.Or,
        AnyShapeModel.And,
        AnyShapeModel.Xone,
        AnyShapeModel.Not)

  protected def findCandidates(): Seq[ValidationCandidate] = {
    val results = mutable.Map[String, Seq[CollectedElement]]()
    val shapes  = mutable.Map[String, Shape]()
    model.findByType((Namespace.Shapes + "Shape").iri()) foreach {
      case shape: AnyShape if shape.meta == AnyShapeModel && !anyShapeRestrictions.exists(shape.fields.exists) => // ignore any shape without logical restriccions, any payload it's valid
      case shape: AnyShape if results.keys.exists(_.equals(shape.id)) =>
        val currentExamples: Seq[CollectedElement] = results(shape.id)
        shape.examples.foreach(e => {
          if (!currentExamples.exists(_.id.equals(e.id))) {
            e match {
              case example: Example
                  if example.fields.exists(ExampleModel.StructuredValue)
                    && example.strict.option().getOrElse(true) && !currentExamples.exists(_.id.equals(example.id)) =>
                results.update(shape.id,
                               currentExamples :+ DataNodeCollectedElement(example.structuredValue,
                                                                           example.id,
                                                                           example.raw.value(),
                                                                           example.annotations))
              case example: Example
                  if example.fields.exists(ExampleModel.Raw)
                    && example.strict.option().getOrElse(true) && !currentExamples.exists(_.id.equals(example.id)) =>
                results.update(
                  shape.id,
                  currentExamples :+ StringCollectedElement(example.id, example.raw.value(), example.annotations))
              case _ =>
            }
          }
        })
        results.update(shape.id, results(shape.id) ++ getDefault(shape))
      case shape: AnyShape =>
        val examples = shape.examples.collect({
          case example: Example
              if example.fields.exists(ExampleModel.StructuredValue) && example.strict.option().getOrElse(true) =>
            DataNodeCollectedElement(example.structuredValue, example.id, example.raw.value(), example.annotations)
          case example: Example
              if example.fields.exists(ExampleModel.Raw) && example.strict.option().getOrElse(true) =>
            StringCollectedElement(example.id, example.raw.value(), example.annotations)
        })
        if (examples.nonEmpty) {
          results.put(shape.id, examples ++ getDefault(shape))
          shapes.put(shape.id, shape)
        } else {
          getDefault(shape) match {
            case Some(ei) =>
              results.put(shape.id, Seq(ei))
              shapes.put(shape.id, shape)
            case _ => // ignore
          }
        }
      case _ =>
    }
    val seq = results
      .flatMap({
        case (id, e) =>
          val shape = shapes(id)
          e.map(e => ValidationCandidate(shape, buildFragment(shape, e)))
      })
      .toSeq
    seq
  }

  private def getDefault(shape: Shape): Option[CollectedElement] = {
    Option(shape.default)
      .map(d => DataNodeCollectedElement(d, d.id, shape.defaultString.option().getOrElse(""), d.annotations))
      .orElse({
        shape.defaultString.option().map { s =>
          StringCollectedElement(shape.id, s, shape.defaultString.annotations())
        }
      })
  }

  private abstract class CollectedElement(val id: String, val raw: String, val a: Annotations)

  private case class DataNodeCollectedElement(dataNode: DataNode,
                                              override val id: String,
                                              override val raw: String,
                                              override val a: Annotations)
      extends CollectedElement(id, raw, a)

  private case class StringCollectedElement(override val id: String,
                                            override val raw: String,
                                            override val a: Annotations)
      extends CollectedElement(id, raw, a)

  private def buildFragment(shape: Shape, colectedElement: CollectedElement) = {
    val fragment = colectedElement match {
      case dn: DataNodeCollectedElement => // the example has been parsed, so i can use native validation like json or any default

        val newNode: DataNode = dn.dataNode match {
          case scalar: ScalarNode
              if shape.fields
                .entry(ScalarShapeModel.DataType)
                .exists(e => e.value.value.toString.equals((Namespace.Xsd + "string").iri())) =>
            val node = scalar.cloneNode()
            node.withId(scalar.id).dataType = Some((Namespace.Xsd + "string").iri())
            node
          case other => other
        }
        PayloadFragment(newNode, "text/vnd.yaml")
      case s: StringCollectedElement =>
        PayloadFragment(ScalarNode(s.raw, None, s.a), s.raw.guessMediaType(shape.isInstanceOf[ScalarShape])) // todo: review with antonio
    }
    fragment.encodes.withId(colectedElement.id)
    fragment
  }

}

object PayloadsInApiCollector {
  def apply(model: BaseUnit): Seq[ValidationCandidate] = new PayloadsInApiCollector(model).collect()
}