package org.scalaprops.ui.editors

import javax.swing.JLabel
import org.scalaprops.ui.util.NamedPanel
import org.scalaprops.ui.{EditorFactory, Editor}
import java.awt.Dimension

case class NoEditorFactory[T] extends EditorFactory[T] {
  protected def createEditorInstance = new NoEditor()
}

/**
 * Just shows toString version of the value.
 */
class NoEditor[T] extends NamedPanel() with Editor[T] {

  private val valueDisplay = new JLabel()

  valueDisplay.setMinimumSize(new Dimension(24, 24))
  add(valueDisplay, "align right")

  protected def onExternalValueChange(oldValue: T, newValue: T) {
    showValue(newValue)
  }

  protected def onInit(initialValue: T, name: String) {
    title = name
    showValue(initialValue)
  }

  private def showValue(v: T) {
    if (v == null) valueDisplay.setText("null")
    else valueDisplay.setText("" + v)
  }

}
