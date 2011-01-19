package org.scalaprops.parser

import scala.reflect.Manifest
import java.io.{StringReader, Reader}
import tools.nsc.io.File
import org.scalaprops.serialization.{Serializers, StandardSerializers}
import org.scalaprops.{BeanFactory, PropertyBean, Bean}
import org.scalaprops.utils.ClassUtils

/**
 * Something that can parse Property Beans.
 */
trait BeanParser {

  /** The property name for fields that indicate what kind of bean the object they are in should be deserialized to. */
  var typePropertyName = 'beanType

  /** Serializers used to parse and write property values of various types */
  var serializers: Serializers = new StandardSerializers

  /** True if properties not already present in the bean should be added to it when reading it */
  var addUnknownProperties = true

  /** Factory used to create bean instances based on the bean type specified in the typeField. */
  var beanFactory: BeanFactory = new BeanFactory()

  /** Parses the input stream and returns a list of the beans found in it */
  def parse(reader: Reader, sourceName: String): Bean

  /** Parses the input text and returns a list of the beans found in it */
  def parse(text: String, sourceName: String): Bean = parse(new StringReader(text), sourceName)

  /** Parses the input file and returns a list of the beans found in it */
  def parse(file: File): Bean = parse(file.reader, file.path)

  protected def createBean(propertyValues: Map[Symbol, AnyRef]): Bean = {
    val bean: Bean = if (propertyValues.contains(typePropertyName)) beanFactory.createBeanInstance(asSymbol(propertyValues(typePropertyName)))
                     else beanFactory.createDefaultBeanInstance()
    propertyValues foreach { e =>
      val field: Symbol = e._1
      var value: AnyRef = e._2
      if (field != typePropertyName) {
        if (bean.contains(field)) {
          // Do any deserialization if needed:
          val kind = bean.properties(field).kind.erasure
          value = serializers.deserialize(kind, value.toString)

          bean.set(field, value)
        }
        else {
          if (addUnknownProperties) {
            val kind = if (value == null) classOf[String] else value.getClass()
            bean.addProperty(field, value)(Manifest.classType(kind))
          }
        }
      }
    }

    bean
  }

  private def asSymbol(value: AnyRef): Symbol = {
    if (value.isInstanceOf[Symbol]) value.asInstanceOf[Symbol]
    else Symbol(value.toString)
  }


}