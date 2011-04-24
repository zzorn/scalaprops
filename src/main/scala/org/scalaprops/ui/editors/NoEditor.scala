package org.scalaprops.ui.editors

import org.scalaprops.ui.Editor
import javax.swing.JLabel
import org.scalaprops.ui.util.NamedPanel

/**
 * Just shows toString version of the value.
 */
class NoEditor extends NamedPanel() with Editor[AnyRef] {

  private val valueDisplay = new JLabel()

  add(valueDisplay, "grow")

  protected def onValueChange(oldValue: AnyRef, newValue: AnyRef) {
    showValue(newValue)
  }

  protected def onInit(initialValue: AnyRef, name: String) {
    title = name
    showValue(initialValue)
  }

  private def showValue(v: AnyRef) {
    if (v == null) valueDisplay.setText("null")
    else valueDisplay.setText(v.toString)
  }

}
