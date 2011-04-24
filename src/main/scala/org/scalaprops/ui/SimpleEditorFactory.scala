package org.scalaprops.ui

/**
 * 
 */
case class SimpleEditorFactory[T](editorKind: Class[Editor[T]]) extends EditorFactory[T] {
  protected def createEditorInstance = editorKind.newInstance
}