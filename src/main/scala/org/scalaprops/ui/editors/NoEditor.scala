package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}
import java.awt.Dimension
import javax.swing.{JPanel}

case class NoEditorFactory[T] extends EditorFactory[T] {
  protected def createEditorInstance = new NoEditor()
}

/**
 * Editor that doesn't show anything.
 */
class NoEditor[T] extends JPanel() with Editor[T] {

  setPreferredSize(new Dimension(0,0))

  protected def onExternalValueChange(oldValue: T, newValue: T) {
  }

  protected def onInit(initialValue: T, name: String) {
  }

}
