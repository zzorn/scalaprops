package org.scalaprops.exporter

import java.io.Writer
import org.scalaprops.{Property, Bean}
import org.scalaprops.serialization.Serializers

/**
 * Exports a bean in JSON format.
 */
object JsonBeanExporter extends BeanExporter {

  def export(bean: Bean, writer: Writer, serializers: Serializers) {
    writeBean(0, bean, writer, serializers)
    writer.append("\n")
  }

  private def writeBean(indent: Int, bean: Bean, writer: Writer, serializers: Serializers) {

    def writeBeanProperty(name: Symbol, value: AnyRef, kind: Manifest[_]) {
      writer.append(spaces(indent + 1)).append("\"").append(name.name).append("\"").append(": ")
      writeValue(indent + 1, writer, value, kind, serializers)
    }

    // Bean start
    writer.append("{\n")

    // Bean type name
    writeBeanProperty(Bean.typePropertyName, bean.beanName.name, null)
    if (bean.properties.size > 0) writer.append(",")
    writer.append("\n")

    // Bean properties
    foreachWithSeparator(bean.properties.values, () => writer.append(",\n")){ e: Property[_] =>
      writeBeanProperty(e.name, e.value.asInstanceOf[AnyRef], e.kind)
    }

    // Bean end
    writer.append("\n").append(spaces(indent)).append("}")
  }

  private def writeValue(indent: Int, writer: Writer, value: AnyRef, kind: Manifest[_], serializers: Serializers) {
    if (value == null) writer.append("null")
    else if (value.isInstanceOf[Bean]) {
      // Check if it is a bean
      writeBean(indent, value.asInstanceOf[Bean], writer, serializers)
    }
    else if (value.isInstanceOf[List[_]]) {
      // Get list element type
      // Lists have one type argument, get it from the manifest if it is present
      val elementType: Manifest[_] = if (kind == null) null else (kind.typeArguments.head)

      // Check if it is a list
      writer.append("[\n")
      foreachWithSeparator(value.asInstanceOf[List[_]], () => writer.append(",\n")){ e =>
        writer.append(spaces(indent + 1))
        writeValue(indent + 1, writer, e.asInstanceOf[AnyRef], elementType, serializers)
      }
      writer.append("\n")
      writer.append(spaces(indent)).append("]")
    }
    else {
      // Use manifest type if available, otherwise get type from object
      val valueType = if (kind == null) value.getClass else kind.erasure

      // Other type, try to serialize it if possible
      writer.append(serializers.serialize(valueType, value))
    }
  }

  private def spaces(indent: Int): String = {
    var s = ""
    var i = 0
    while (i < indent) {
      s = s + "  "
      i += 1
    }
    s
  }

  private def foreachWithSeparator[T](iterable: Iterable[T], separator: () => Any)(handler: T => Any) {
    val num = iterable.size
    var i = 0
    iterable.foreach({e =>
      handler(e)
      i += 1
      if (i < num) separator()
    })
  }

}