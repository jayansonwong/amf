package amf.model

import amf.plugins.domain.shapes.models

import scala.collection.JavaConverters._

case class FileShape(private val file: models.FileShape) extends Shape(file) {

  val fileTypes: java.util.List[String] = file.fileTypes.asJava
  val pattern: String                   = file.pattern
  val minLength: Int                    = file.minLength
  val maxLength: Int                    = file.maxLength
  val minimum: String                   = file.minimum
  val maximum: String                   = file.maximum
  val exclusiveMinimum: String          = file.exclusiveMinimum
  val exclusiveMaximum: String          = file.exclusiveMaximum
  val format: String                    = file.format
  val multipleOf: Int                   = file.multipleOf

  def withFileTypes(fileTypes: java.util.List[String]): this.type = {
    file.withFileTypes(fileTypes.asScala)
    this
  }
  def withPattern(pattern: String): this.type = {
    file.withPattern(pattern)
    this
  }
  def withMinLength(min: Int): this.type = {
    file.withMinLength(min)
    this
  }
  def withMaxLength(max: Int): this.type = {
    file.withMaxLength(max)
    this
  }
  def withMinimum(min: String): this.type = {
    file.withMinimum(min)
    this
  }
  def withMaximum(max: String): this.type = {
    file.withMaximum(max)
    this
  }
  def withExclusiveMinimum(min: String): this.type = {
    file.withExclusiveMinimum(min)
    this
  }
  def withExclusiveMaximum(max: String): this.type = {
    file.withExclusiveMaximum(max)
    this
  }
  def withFormat(format: String): this.type = {
    file.withFormat(format)
    this
  }
  def withMultipleOf(multiple: Int): this.type = {
    file.withMultipleOf(multiple)
    this
  }

  override private[amf] def element = file

  override def linkTarget: Option[FileShape] =
    element.linkTarget.map({ case l: models.FileShape => FileShape(l) })

  override def linkCopy(): FileShape = FileShape(element.linkCopy())
}
