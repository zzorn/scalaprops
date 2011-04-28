package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}

class BoolEditorFactory extends EditorFactory[Boolean] {
  protected def createEditorInstance = new BoolEditor()
}

/**
 * 
 */
class BoolEditor extends Editor[Boolean] {
  protected def onExternalValueChange(oldValue: Boolean, newValue: Boolean) = null

  protected def onInit(initialValue: Boolean, name: String) = null
}