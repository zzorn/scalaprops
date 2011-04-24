package org.scalaprops.ui

import org.scalaprops.Property

/**
 * Used to specify general settings for the editor of some specific property.
 */
trait EditorFactory[T] {

  protected def createEditorInstance: Editor[T]

  final def createEditor(property: Property[T]): Editor[T] = {
    val editor = createEditorInstance
    editor.init(property)
    editor
  }

}
