package org.scalaprops.ui.editors

import org.scalaprops.ui.util.TitledPanel
import javax.swing.{JPanel, JComponent}
import net.miginfocom.swing.MigLayout
import org.scalaprops.{Property, BeanListener, Bean}
import java.awt.BorderLayout
import org.scalaprops.ui.{EditorFactory, Editor}

class BeanEditorFactory[T <: Bean] extends EditorFactory[T] {
  protected def createEditorInstance = new BeanEditor
}

/**
 * An UI for editing beans.
 */
class BeanEditor[T <: Bean] extends TitledPanel() with Editor[T] {

  private var propertyEditors: List[Editor[_]] = Nil

  protected def updateView() = null

  private val beanListener: BeanListener = new BeanListener() {
    def onPropertyAdded(bean: Bean, property: Property[ _ ]) {
      addPropertyUi(property)
    }

    def onPropertyRemoved(bean: Bean, property: Property[ _ ]) {
      removePropertyUi(property)
    }

    def onPropertyChanged(bean: Bean, property: Property[ _ ]) {}
  }

  setLayout(new MigLayout("wrap 1, fillx, insets 0","[grow]","0[]0[]0"))

  protected def onExternalValueChange(oldValue: T, newValue: T) {
    if (oldValue != null) oldValue.removeListener(beanListener)

    // Remove old UI
    removeUi()

    // Rebuild new UI
    if (newValue != null) {
      buildUi(newValue)
      newValue.addListener(beanListener)
    }
  }

  protected def onInit(bean: T, name: String) {
    title = name

    if (bean != null) {
      buildUi(bean)
      bean.addListener(beanListener)
    }
  }

  private def buildUi(bean: T) {
    bean.properties.values foreach (p => addPropertyUi(p))
  }

  private def addPropertyUi(property: Property[_]) {
    val editor = property.createEditor
    propertyEditors ::= editor
    add(editor, "width 100%")
  }

  private def removePropertyUi(property: Property[_]) {
    propertyEditors.find(_.property == property).foreach{editor =>
      remove(editor)
      editor.deInit()
      propertyEditors = propertyEditors.filterNot(_ == editor)
    }
  }

  private def removeUi() {
    removeAll()
    propertyEditors.foreach(_.deInit())
  }

  override protected def onDeInit() {
    removeUi()
    if (value != null) value.removeListener(beanListener)
  }
}
