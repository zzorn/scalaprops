package org.scalaprops.exporter

import org.scalaprops.Bean
import tools.nsc.io.File
import java.io.{BufferedWriter, StringWriter, Writer}
import org.scalaprops.serialization.{StandardSerializers, Serializers}

/**
 * Trait for something that can be used to export beans in some form.
 */
trait BeanExporter {

  /**
   * Write the bean to the specified writer.
   */
  def export(bean: Bean, writer: Writer, serializers: Serializers)

}