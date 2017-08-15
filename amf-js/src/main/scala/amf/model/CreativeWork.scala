package amf.model

import amf.model.builder.CreativeWorkBuilder

import scala.scalajs.js.annotation.JSExportAll

/**
  * CreativeWork js class
  */
@JSExportAll
case class CreativeWork private[model] (private val creativeWork: amf.domain.CreativeWork) extends DomainElement {

  val url: String = creativeWork.url

  val description: String = creativeWork.description

  def toBuilder: CreativeWorkBuilder = CreativeWorkBuilder(creativeWork.toBuilder)

  override def equals(other: Any): Boolean = other match {
    case that: CreativeWork =>
      (that canEqual this) &&
        creativeWork == that.creativeWork
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[CreativeWork]

  override private[amf] def element: amf.domain.CreativeWork = creativeWork
}
