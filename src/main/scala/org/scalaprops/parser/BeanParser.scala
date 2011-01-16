package org.scalaprops.parser

import org.scalaprops.{PropertyBean, Bean}
import scala.reflect.Manifest
import java.io.{StringReader, Reader}
import tools.nsc.io.File

/**
 * Something that can parse Property Beans.
 */
trait BeanParser {

  type BeanConstructor = () => _ <: Bean
  type BeanCreator = Symbol => _ <: Option[Bean]
  type Deserializer = String => AnyRef

  private var beanConstructors: Map[Symbol, BeanConstructor] = Map()
  private val initialBeanCreator: BeanCreator = { (name: Symbol) => beanConstructors.get(name).flatMap(x => Some(x())) }
  private var beanCreators: List[BeanCreator] = List(initialBeanCreator)
  private var defaultBeanConstructor: BeanConstructor = {() => new PropertyBean()}
  private var deserializers: Map[Class[_], Deserializer] = Map()

  registerDeserializer[java.lang.Boolean] {s => new java.lang.Boolean(s)}
  registerDeserializer[java.lang.Byte] {s => new java.lang.Byte(s)}
  registerDeserializer[java.lang.Short] {s => new java.lang.Short(s)}
  registerDeserializer[java.lang.Integer] {s => new java.lang.Integer(s)}
  registerDeserializer[java.lang.Long] {s => new java.lang.Long(s)}
  registerDeserializer[java.lang.Float] {s => new java.lang.Float(s)}
  registerDeserializer[java.lang.Double] {s => new java.lang.Double(s)}
  registerDeserializer[java.lang.String] {s => s}

  registerDeserializer[Boolean] {s => new java.lang.Boolean(s)}
  registerDeserializer[Byte] {s => new java.lang.Byte(s)}
  registerDeserializer[Short] {s => new java.lang.Short(s)}
  registerDeserializer[Int] {s => new java.lang.Integer(s)}
  registerDeserializer[Long] {s => new java.lang.Long(s)}
  registerDeserializer[Float] {s => new java.lang.Float(s)}
  registerDeserializer[Double] {s => new java.lang.Double(s)}

  registerDeserializer[Symbol] {s => Symbol(s)}

  var addUnknownFields = true

  def registerDeserializer[T](deserializer: Deserializer)(implicit target: Manifest[T]) = deserializers += (target.erasure -> deserializer)
  def registerBeanType(typeName: Symbol, createInstance: BeanConstructor) = beanConstructors += (typeName -> createInstance)
  def registerBeanTypes(creator: BeanCreator) = beanCreators ::= creator
  def registerDefaultBeanType(createInstance: BeanConstructor) = defaultBeanConstructor = createInstance

  /** The property name for fields that indicate what kind of bean the object they are in should be deserialized to. */
  var typeFieldName = 'beanType

  /** Parses the input stream and returns a list of the beans found in it */
  def parse(reader: Reader, sourceName: String): Bean

  /** Parses the input text and returns a list of the beans found in it */
  def parse(text: String, sourceName: String): Bean = parse(new StringReader(text), sourceName)

  /** Parses the input file and returns a list of the beans found in it */
  def parse(file: File): Bean = parse(file.reader, file.path)

  protected def deserialize(value: String, target: Class[_]): AnyRef = {
    deserializers.get(target).map(ds => ds(value)).getOrElse(value)
  }

  protected def createBean(propertyValues: Map[Symbol, AnyRef]): Bean = {
    val bean: Bean = if (propertyValues.contains(typeFieldName)) createBeanInstance(asSymbol(propertyValues(typeFieldName)))
                     else defaultBeanConstructor()
    propertyValues foreach { e =>
      val field: Symbol = e._1
      var value: AnyRef = e._2
      if (field != typeFieldName) {
        if (bean.contains(field)) {
          // Do any deserialization if needed:
          value = deserialize(value.toString, bean.properties(field).kind.erasure)

          bean.set(field, value)
        }
        else {
          if (addUnknownFields) {
            val kind = if (value == null) classOf[String] else value.getClass()
            bean.addProperty(field, value)(Manifest.classType(kind))
          }
        }
      }
    }

    bean
  }

  protected def createBeanInstance(typeName: Symbol): Bean = {
    var bean: Bean = null
    beanCreators exists { bc =>
      bc(typeName) match  {
        case None =>
          // TODO: Logging?
          println("No bean creator found for bean type " + typeName + ", using default bean type.")
          false
        case Some(b) =>
          bean = b
          true
      }
    }

    if (bean == null) bean = defaultBeanConstructor()
    bean
  }

  private def asSymbol(value: AnyRef): Symbol = {
    if (value.isInstanceOf[Symbol]) value.asInstanceOf[Symbol]
    else Symbol(value.toString)
  }


}