package org.scalaprops.ui

import org.scalaprops.ui.editors._
import org.scalaprops.utils.ClassUtils
import org.scalaprops.Property

/**
 * 
 */
object DefaultEditorFactory {

  private val STRING = classOf[String].getName
  private val BOOL = classOf[java.lang.Boolean].getName
  private val BYTE = classOf[java.lang.Byte].getName
  private val SHORT = classOf[java.lang.Short].getName
  private val INT = classOf[java.lang.Integer].getName
  private val LONG = classOf[java.lang.Long].getName
  private val FLOAT = classOf[java.lang.Float].getName
  private val DOUBLE = classOf[java.lang.Double].getName

  def createEditorFor[T](kind: Class[T], property: Property[T]): Editor[T] = {
    val wrappedKind = ClassUtils.nativeTypeToWrappedType(kind)
    val editor = wrappedKind.getName match {
      case STRING => (new StringEditorFactory()).apply(property.asInstanceOf[Property[String]])
      case BOOL => (new BoolEditorFactory()).apply(property.asInstanceOf[Property[Boolean]])
      case BYTE => (new ByteEditorFactory).apply(property.asInstanceOf[Property[Byte]])
      case SHORT => (new ShortEditorFactory).apply(property.asInstanceOf[Property[Short]])
      case INT => (new IntEditorFactory).apply(property.asInstanceOf[Property[Int]])
      case LONG => (new LongEditorFactory).apply(property.asInstanceOf[Property[Long]])
      case FLOAT => (new FloatEditorFactory).apply(property.asInstanceOf[Property[Float]])
      case DOUBLE => (new DoubleEditorFactory).apply(property.asInstanceOf[Property[Double]])
      case _ => (new NoEditorFactory[T]()).apply(property.asInstanceOf[Property[T]])
    }

    editor.asInstanceOf[Editor[T]]
  }

}