package org.scalaprops.ui.editors

import org.scalaprops.ui.util.NamedPanel
import org.scalaprops.ui.{EditorFactory, Editor}
import scala.collection.JavaConversions._
import javax.swing.{JComboBox}
import scala.Array
import java.awt.event.{MouseWheelEvent, MouseWheelListener, ActionEvent, ActionListener}

class SelectionEditorFactory[T <: AnyRef](allowedValues: List[T], labelMaker: (T) => String = {(t: T) => t.toString}) extends EditorFactory[T] {
  protected def createEditorInstance = new SelectionEditor(allowedValues, labelMaker)
}

/**
 * Select value from a drop-down list.
 */
class SelectionEditor[T <: AnyRef](allowedValues: List[T], labelMaker: (T) => String = {(t: T) => t.toString}) extends NamedPanel with Editor[T] {

  private def makeLabels(values: List[T]): Array[AnyRef] = {
    val array = new Array[AnyRef](values.length)
    var i = 0
    values foreach {v => array(i) = labelMaker(v); i += 1}
    array
  }

  private val comboBox: JComboBox = new JComboBox(makeLabels(allowedValues))

  comboBox.addActionListener(new ActionListener{
    def actionPerformed(e: ActionEvent) {
      val index = comboBox.getSelectedIndex

      if (index >= 0) onEditorChange(allowedValues(index))
    }
  })

  comboBox.addMouseWheelListener(new MouseWheelListener{
    def mouseWheelMoved(e: MouseWheelEvent) {
      val newIndex = comboBox.getSelectedIndex + e.getWheelRotation
      val clamp = if (newIndex < 0) 0
                  else if (newIndex >= allowedValues.size) allowedValues.size - 1
                  else newIndex
      comboBox.setSelectedIndex(clamp)
    }
  })

  add(comboBox, "width 100%, align right")

  protected def onExternalValueChange(oldValue: T, newValue: T) {
    val index = allowedValues.indexOf(newValue)
    comboBox.setSelectedIndex(index)
  }

  protected def onInit(initialValue: T, name: String) {
    val index = allowedValues.indexOf(initialValue)
    comboBox.setSelectedIndex(index)
  }
}