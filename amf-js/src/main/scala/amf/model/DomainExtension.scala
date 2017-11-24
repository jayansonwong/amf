package amf.model

import amf.plugins.domain.webapi.models.extensions

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class DomainExtension(private[amf] val domainExtension: extensions.DomainExtension)
    extends DomainElement {

  def name: String                    = domainExtension.name
  def definedBy: CustomDomainProperty = CustomDomainProperty(domainExtension.definedBy)
  def extension: DataNode             = DataNode(domainExtension.extension)

  def withName(name: String): this.type = {
    domainExtension.withName(name)
    this
  }

  def withDefinedBy(customDomainProperty: CustomDomainProperty): this.type = {
    domainExtension.withDefinedBy(customDomainProperty.customDomainProperty)
    this
  }

  def withExtension(dataNode: DataNode): this.type = {
    domainExtension.withExtension(dataNode.dataNode)
    this
  }

  override private[amf] def element = domainExtension

  def this() = this(extensions.DomainExtension())
}
