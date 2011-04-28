package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}
import java.awt.Dimension
import javax.swing.{JPanel}

case class NoEditorFactory extends EditorFactory {
  protected def createEditorInstance = new NoEditor()
}

/**
 * Editor that doesn't show anything.
 */
class NoEditor extends JPanel() with Editor[Any] {

  setPreferredSize(new Dimension(0,0))

  protected def onExternalValueChange(oldValue: Any, newValue: Any) {
  }

  protected def onInit(initialValue: Any, name: String) {
  }

}
