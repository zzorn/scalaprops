package org.scalaprops.ui.editors

import org.scalaprops.ui.Editor
import org.scalaprops.ui.util.TitledPanel
import javax.swing.{JPanel, JComponent}
import net.miginfocom.swing.MigLayout
import org.scalaprops.{Property, BeanListener, Bean}
import java.awt.BorderLayout

/**
 * An UI for editing beans.
 */
class BeanEditor extends TitledPanel(constraints = "wrap 1") with Editor[Bean] {

  private var propertyEditors: List[Editor[_]] = Nil

  protected def updateView() = null

  private val beanListener: BeanListener = new BeanListener() {
    def onPropertyAdded(bean: Bean, property: Property[ _ ]) {
      addPropertyUi(property)
    }

    def onPropertyRemoved(bean: Bean, property: Property[ _ ]) {
      removePropertyUi(property)
    }
  }

  protected def onValueChange(oldValue: Bean, newValue: Bean) {
    if (oldValue != null) oldValue.removeListener(beanListener)

    // Remove old UI
    removeUi()

    // Rebuild new UI
    if (newValue != null) {
      buildUi(newValue)
      newValue.addListener(beanListener)
    }
  }

  protected def onInit(bean: Bean, name: String) {
    title = name

    if (bean != null) {
      buildUi(bean)
      bean.addListener(beanListener)
    }
  }

  private def buildUi(bean: Bean) {
    bean.properties.values foreach (p => addPropertyUi(p))
  }

  private def addPropertyUi(property: Property[_]) {
    val editor = property.createEditor
    propertyEditors ::= editor
    add(editor)
  }

  private def removePropertyUi(property: Property[_]) {
    propertyEditors.find(_.property == property).foreach{editor =>
      remove(editor)
      editor.deInit
      propertyEditors = propertyEditors.filterNot(_ == editor)
    }
  }

  private def removeUi() {
    removeAll
    propertyEditors.foreach(_.deInit)
  }

  override protected def onDeInit() {
    removeUi()
    if (value != null) value.removeListener(beanListener)
  }
}