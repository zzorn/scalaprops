package org.scalaprops.ui

import org.scalaprops.ui.editors._

/**
 * 
 */
object DefaultEditorFactory {

  private val STRING = classOf[String].getName
  private val BOOL = classOf[Boolean].getName
  private val BYTE = classOf[Byte].getName
  private val SHORT = classOf[Short].getName
  private val INT = classOf[Int].getName
  private val LONG = classOf[Long].getName
  private val FLOAT = classOf[Float].getName
  private val DOUBLE = classOf[Double].getName

  def factoryFor[T](kind: Class[T]): EditorFactory[T] = {
    val factoryType = kind.getName match {
      case STRING => classOf[StringEditor]
      case BOOL => classOf[BoolEditor]
      case BYTE => classOf[NumberEditor]
      case SHORT => classOf[NumberEditor]
      case INT => classOf[NumberEditor]
      case LONG => classOf[NumberEditor]
      case FLOAT => classOf[NumberEditor]
      case DOUBLE => classOf[NumberEditor]
      case _ => classOf[NoEditor]
    }
    new SimpleEditorFactory(factoryType.asInstanceOf[Class[Editor[T]]])
  }

}