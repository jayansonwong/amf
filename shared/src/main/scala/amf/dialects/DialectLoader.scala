package amf.dialects

import amf.dialects.RAML_1_0_DialectTopLevel.NodeDefinitionObject
import amf.dialects.RAML_1_0_DialectTopLevel.PropertyMappingObject
import amf.document.{BaseUnit, Document}
import amf.metadata.Type
import amf.vocabulary.Namespace

import scala.collection.mutable

/**
  * Created by Pavel Petrochenko on 15/09/17.
  */

case class NM(namespace: Namespace, name:String){ }
object NM{
  def apply(s:String): Option[NM] ={

    val ind: Int = Math.max(s.lastIndexOf('/'), s.lastIndexOf('#'))

    if (ind > 0) {
      val namespace = Namespace(s.substring(0, ind + 1))
      val str1 = s.substring(ind + 1)
      Some(NM(namespace,str1))
    } else {
      None
    }
  }
}

class DialectLoader {

  val builtins = new TypeBuiltins()

  private def retrieveDomainEntity(unit:BaseUnit) = unit match {
    case document: Document => document.encodes.asInstanceOf[DomainEntity]
    case _                  => throw new Exception(s"Cannot load a dialect from a unit that is not a document $unit")
  }

  def loadDialect(document: BaseUnit): Dialect = loadDialect(retrieveDomainEntity(document))

  def loadDialect(domainEntity: DomainEntity): Dialect = {
    val dialectObject=RAML_1_0_DialectTopLevel.dialectObject(domainEntity);
    val rootEntity = for {
      ramlNode     <- dialectObject.raml
      ramlDocument <- ramlNode.document
      root         <- ramlDocument.encodes
    } yield {
      root
    }

    rootEntity match {

      case Some(encodedRootEntity) =>
        val dialectMap = mutable.Map[String,DialectNode]()
        val propertyMap = mutable.Map[DialectPropertyMapping,PropertyMappingObject]()

        dialectObject.nodeMappings.foreach { n =>
          NM(n.classTerm.get) match {
            case Some(ns) =>
              dialectMap.put(n.entity.id, new DialectNode(ns.name, ns.namespace));
            case _        => // ignore
          }
        }

        dialectObject.nodeMappings.foreach {  n =>
          parseNodeMapping(n, dialectMap,propertyMap)
        }
        fillHashes( propertyMap)
        val dialect = for {
          dialectName    <- dialectObject.dialect()
          dialectVersion <- dialectObject.version()
          dialectNode    <- dialectMap.get(encodedRootEntity)
        } yield {
          Dialect(dialectName, dialectVersion, dialectNode)
        }

        dialect match {
          case Some(dialectFound) => dialectFound
          case _                  => throw new Exception("Cannot load dialect, dialect not found")
        }

      case _ => throw new Exception("Cannot load dialect, root entity not found")
    }
  }




  private def fillHashes(propertyMap: mutable.Map[DialectPropertyMapping, PropertyMappingObject]) = {
    propertyMap.keys.foreach(dialectPropertyMapping =>
      propertyMap.get(dialectPropertyMapping).foreach(v => v.hash.map(hash => dialectPropertyMapping.rangeAsDialect.map(rangeNode=>
          rangeNode.props.values.filter(_.iri() == hash).foreach(connectHash(dialectPropertyMapping,  _))
        )
    )))
  }

  private def connectHash(hashedProperty: DialectPropertyMapping,  hashProperty: DialectPropertyMapping) = {
    hashedProperty.owningNode.get.add(hashedProperty.copy(hash = Option(hashProperty)))
    hashedProperty.range.asInstanceOf[DialectNode].add(hashProperty.copy(noRAML = true))
  }

  def parsePropertyMapping(domainEntity: PropertyMappingObject, dialects: mutable.Map[String,DialectNode]): DialectPropertyMapping = {

    val name = domainEntity.name
    val `type`: Type = resolveType(domainEntity, dialects)

    var res =  DialectPropertyMapping(name.get,`type`)

    domainEntity.mandatory.foreach { mandatory =>
      res = res.copy(required = mandatory)
    }

    domainEntity.allowMultiple.foreach { isCollection =>
      res = res.copy(collection = isCollection)
    }

    domainEntity.propertyTerm.foreach { term =>
      NM(term) foreach { ns =>
        res = res.copy(namespace = Some(ns.namespace), rdfName = Some(ns.name))
      }
    }

    // ??
    /*
    val enum    = domainEntity.strings(PropertyMapping.enum)
    val pattern = domainEntity.string(PropertyMapping.pattern)
    val minimum = domainEntity.string(PropertyMapping.minimum)
    val maximum = domainEntity.string(PropertyMapping.maximum)
    val hash    = domainEntity.string(PropertyMapping.hash)
    val asMap   = domainEntity.boolean(PropertyMapping.asMap)
    */

    res
  }

  private def resolveType(domainEntity: PropertyMappingObject, dialects: mutable.Map[String, DialectNode]) = {
    val range = domainEntity.range.getOrElse(TypeBuiltins.STRING)

    builtins.buitInType(range) match {
      case Some(t) => t
      case None =>
        dialects.get(range) match {
          case Some(t) => t
          case None => throw new Exception(s"Cannot find dialect node type for $range")
        }
    }
  }

  def parseNodeMapping(domainEntity: NodeDefinitionObject, dialects: mutable.Map[String,DialectNode], props: mutable.Map[DialectPropertyMapping,PropertyMappingObject]): DialectNode = {
    val node = dialects(domainEntity.entity.id)

    domainEntity.mapping.foreach { p=>
      val mapping = node.add(parsePropertyMapping(p, dialects))
      props.put(mapping,p);
    }

    node
  }


}