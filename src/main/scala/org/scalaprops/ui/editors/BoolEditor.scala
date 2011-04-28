package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}
import javax.swing.event.{ChangeEvent, ChangeListener}
import org.scalaprops.ui.util.NamedPanel
import javax.swing.{JPanel, JCheckBox}
import net.miginfocom.swing.MigLayout

class BoolEditorFactory extends EditorFactory[Boolean] {
  protected def createEditorInstance = new BoolEditor()
}

/**
 * 
 */
class BoolEditor extends NamedPanel with Editor[Boolean] {
  private val checkbox: JCheckBox = new JCheckBox()

  checkbox.addChangeListener(new ChangeListener{
    def stateChanged(e: ChangeEvent) {
      onEditorChange(checkbox.isSelected)
    }
  })

  add(checkbox, "align right")

  protected def onExternalValueChange(oldValue: Boolean, newValue: Boolean) {
    checkbox.setSelected(newValue)
  }

  protected def onInit(initialValue: Boolean, name: String) {
    checkbox.setSelected(initialValue)
    //checkbox.setText(name)
  }
}