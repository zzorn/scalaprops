package org.scalaprops.ui

import org.scalaprops.Property

/**
 * Used to specify general settings for the editor of some specific property.
 */
trait EditorFactory[T] extends ((Property[T]) => Editor[T]) {

  final def apply(property: Property[T]): Editor[T] = {
    val editor = createEditorInstance
    editor.init(property)
    editor
  }

  protected def createEditorInstance: Editor[T]

}
