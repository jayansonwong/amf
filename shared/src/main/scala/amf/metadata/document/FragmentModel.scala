package amf.metadata.document

import amf.metadata.Field
import amf.metadata.domain.{DomainElementModel, UserDocumentationModel}
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Fragment metamodel
  */
trait FragmentModel extends BaseUnitModel {

  val Encodes = Field(DomainElementModel, Document + "encodes")
}

object FragmentModel extends FragmentModel {

  override val `type`: List[ValueType] = List(Document + "Fragment") ++ BaseUnitModel.`type`

  override val fields: List[Field] = Encodes :: BaseUnitModel.fields
}

object DocumentationItemModel extends FragmentModel {

  val UserDocumentation = Field(UserDocumentationModel, Document + "UserDocumentation")

  override val fields: List[Field]     = UserDocumentation :: FragmentModel.fields
  override val `type`: List[ValueType] = FragmentModel.`type`
}
