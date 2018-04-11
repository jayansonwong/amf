package amf.plugins.domain.shapes.models

import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.UnionShapeModel
import amf.plugins.domain.shapes.metamodel.UnionShapeModel._
import org.yaml.model.YPart

case class UnionShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  def anyOf: Seq[Shape] = fields.field(AnyOf)

  def withAnyOf(elements: Seq[Shape]): this.type = this.setArray(AnyOf, elements)

  override def linkCopy(): AnyShape = UnionShape().withId(id)

  override def meta = UnionShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/union/" + name.option().getOrElse("default-union")
}

object UnionShape {

  def apply(): UnionShape = apply(Annotations())

  def apply(ast: YPart): UnionShape = apply(Annotations(ast))

  def apply(annotations: Annotations): UnionShape = UnionShape(Fields(), annotations)
}
