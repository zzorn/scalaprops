package org.scalaprops.parser

import scala.reflect.Manifest
import org.scalaprops.serialization.{Serializers, StandardSerializers}
import org.scalaprops.utils.ClassUtils
import java.io.{FileReader, File, StringReader, Reader}
import org.scalaprops.{BeanFactory, PropertyBean, Bean}

/**
 * Something that can parse Property Beans.
 */
trait BeanParser {

  /**
   * Parses the input reader and returns a list of the beans found in it
   */
  def parse(reader: Reader,
            sourceName: String,
            beanFactory: BeanFactory,
            serializers: Serializers): Bean

}