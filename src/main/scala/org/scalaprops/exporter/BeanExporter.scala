package org.scalaprops.exporter

import org.scalaprops.Bean
import tools.nsc.io.File
import java.io.{BufferedWriter, StringWriter, Writer}
import org.scalaprops.serialization.{StandardSerializers, Serializers}

/**
 * Trait for something that can be used to export beans in some form.
 */
trait BeanExporter {

  /** The property name for fields that indicate what kind of bean the object they are in should be deserialized to. */
  var typePropertyName = 'beanType

  /** Serializers used to parse and write property values of various types */
  var serializers: Serializers = new StandardSerializers

  def export(bean: Bean, writer: Writer)

  def exportAsString(bean: Bean): String = {
    val stringWriter: StringWriter = new StringWriter()
    export(bean, stringWriter)
    stringWriter.toString
  }

  def export(bean: Bean, file: File, append: Boolean = false) {
    export(bean, new BufferedWriter(file.writer(append)))
  }

}