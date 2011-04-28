package org.scalaprops.ui.editors

import org.scalaprops.ui.Editor
import javax.swing.JLabel
import org.scalaprops.ui.util.NamedPanel

/**
 * Just shows toString version of the value.
 */
class NoEditor[T] extends NamedPanel() with Editor[T] {

  private val valueDisplay = new JLabel()

  add(valueDisplay, "grow, width 100%")

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
