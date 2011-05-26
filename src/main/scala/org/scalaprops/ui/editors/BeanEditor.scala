package org.scalaprops.ui.editors

import org.scalaprops.ui.util.TitledPanel
import net.miginfocom.swing.MigLayout
import org.scalaprops.{Property, BeanListener, Bean}
import org.scalaprops.ui.{EditorFactory, Editor}
import java.awt.{Rectangle, BorderLayout}
import javax.swing._

class BeanEditorFactory[T <: Bean] extends EditorFactory[T] {
  protected def createEditorInstance = new BeanEditor
}

/**
 * An UI for editing beans.
 */
class BeanEditor[T <: Bean] extends TitledPanel() with Editor[T] {

  private var scrollPane: JScrollPane = null
  private var mainPanel: JPanel = null

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

  //setLayout(new MigLayout("fill"))


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

  /**
   * Initialized this editor.  Should be called only once.
   */
  private[scalaprops] final def initForBean(bean: T) {
    // TODO: Camel case to space separated
    title = bean.beanType.name

    init(null)
    valueChanged(null.asInstanceOf[T], bean)
  }

  protected def onInit(bean: T, name: String) {
    title = name

    if (bean != null) {
      buildUi(bean)
      bean.addListener(beanListener)
    }
  }

  private def buildUi(bean: T) {
    mainPanel = new JPanel(new MigLayout()) with Scrollable {
      def getScrollableTracksViewportHeight = false
      def getScrollableTracksViewportWidth = true
      def getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 300
      def getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 30
      def getPreferredScrollableViewportSize = getPreferredSize
    }

    mainPanel.addMouseListener(bean.createContextMenuOpener())

    mainPanel.setLayout(new MigLayout("wrap 1, fillx, insets 0","[grow]","0[]0[]0"))
    scrollPane = new JScrollPane(mainPanel,
                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

    add(scrollPane, "width 100%")

    bean.properties.values foreach (p => addPropertyUi(p))
  }

  private def addPropertyUi(property: Property[_]) {
    val editor = property.createEditor
    propertyEditors ::= editor
    mainPanel.add(editor, "width 100%")
  }

  private def removePropertyUi(property: Property[_]) {
    propertyEditors.find(_.property == property).foreach{editor =>
      mainPanel.remove(editor)
      editor.deInit()
      propertyEditors = propertyEditors.filterNot(_ == editor)
    }
  }

  private def removeUi() {
    if (mainPanel != null) {
      mainPanel.removeAll()
    }
    propertyEditors.foreach(_.deInit())
  }

  override protected def onDeInit() {
    removeUi()
    if (value != null) value.removeListener(beanListener)
  }
}
