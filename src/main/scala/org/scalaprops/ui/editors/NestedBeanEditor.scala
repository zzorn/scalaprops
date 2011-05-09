package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}
import org.scalaprops.ui.util.TitledPanel
import javax.swing.{JPanel, JTree, JSplitPane}
import javax.swing.event.{TreeSelectionEvent, TreeSelectionListener, TreeModelListener}
import java.awt.{Dimension, BorderLayout}
import org.scalaprops.{Property, Bean}
import javax.swing.tree.{DefaultTreeCellRenderer, TreeCellRenderer, TreePath, TreeModel}
import java.util.HashMap

class NestedBeanEditorFactory[T <: Bean] extends EditorFactory[T] {
  override def createEditorInstance: Editor[T] = new NestedBeanEditor[T]()
}

/**
 * 
 */
class NestedBeanEditor[T <: Bean] extends TitledPanel() with Editor[T] {

  var view: JSplitPane = null
  var beanEditorPanel: JPanel = null
  var currentBeanEditor: BeanEditor[_] = null
  var beanSelector: JTree = null

  // TODO: Kind of ugly hack, inprove later so that it holds up to structure changes
  private var names: HashMap[Bean, String] = new HashMap[Bean, String]()

  private def updateNames(root: Bean) {
    if (root == null) names.clear()
    else {
      root.properties.values foreach {p =>
        val child = asChildBean(p)
        if (child != null) {
          names.put(child, p.name.name)
          updateNames(child)
        }
      }
    }
  }

  protected def onExternalValueChange(oldValue: T, newValue: T) {
    // TODO: Implement listener
    //if (oldValue != null) oldValue.removeListener(beanListener)

    // Remove old UI
    removeUi()

    // Rebuild new UI
    if (newValue != null) {
      buildUi(newValue)
      // TODO: Implement listener
      //newValue.addListener(beanListener)
    }
    updateNames(newValue)
  }

  protected def onInit(bean: T, name: String) {
    title = name

    if (bean != null) {
      buildUi(bean)

      // TODO: Implement listener that reacts properly when properties are changed, added or removed
      //bean.addListener(beanListener)
    }

    updateNames(bean)
  }

  /**
   * Initialized this editor.  Should be called only once.
   */
  private[scalaprops] final def initForBean(bean: T) {
    // TODO: Camel case to space separated
    title = bean.beanName.name

    init(null)
    valueChanged(null.asInstanceOf[T], bean)
  }


  private def buildUi(bean: T) {
    beanEditorPanel = new JPanel(new BorderLayout())
    beanEditorPanel.setPreferredSize(new Dimension(400, 500))
    beanSelector = new JTree(createTreeModel) {
      override def convertValueToText(value: AnyRef,
                                      selected: Boolean,
                                      expanded: Boolean,
                                      leaf: Boolean,
                                      row: Int,
                                      hasFocus: Boolean): String = {
        
        val bean = value.asInstanceOf[Bean]
        if (bean == null) "null"
        else {
          val name = names.get(bean)
          if (name == null) bean.toString else name
        }
      }
    }
    beanSelector.setPreferredSize(new Dimension(300, 500))
    expandAll()
    view = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, beanSelector, beanEditorPanel)
    view.setResizeWeight(0.3)
    add(view, "width 100%, height 100%")

    beanSelector.addTreeSelectionListener(new TreeSelectionListener{
      def valueChanged(e: TreeSelectionEvent) {
        val selectedBean = e.getNewLeadSelectionPath.getLastPathComponent.asInstanceOf[Bean]
        updateBeanEditor(selectedBean)
      }
    })
  }

  /**
   * Expands all nodes of the outline tree.
   */
  def expandAll() {
    var row = 0
    while (row < beanSelector.getRowCount) {
        beanSelector.expandRow(row)
        row += 1
    }
  }

  /**
   * Collapses all open nodes of the outline tree.
   */
  def collapseAll() {
    var row = beanSelector.getRowCount - 1
    while (row >= 0) {
        beanSelector.collapseRow(row)
        row -= 1
    }
  }

  private def removeUi() {
    // TODO: Better removal, if needed
    if (view != null) {
      view.removeAll()
    }

    treeListeners = Nil
  }

  private def updateBeanEditor(selectedBean: Bean) {
    beanEditorPanel.removeAll()
    beanEditorPanel.add(selectedBean.createEditor(), BorderLayout.CENTER)
    beanEditorPanel.repaint()
    beanEditorPanel.revalidate()
    repaint()
  }


  // TODO: Notify tree when beans added / removed in the structure
  private var treeListeners: List[TreeModelListener] = Nil

  def createTreeModel: TreeModel = new TreeModel() {

    def addTreeModelListener(l: TreeModelListener) { treeListeners ::= l }
    def removeTreeModelListener(l: TreeModelListener) { treeListeners = treeListeners filterNot ( _ == l)}
    def getIndexOfChild(parent: Object, child: Object): Int = indexOfChildBeanInBean(parent.asInstanceOf[Bean], child.asInstanceOf[Bean])
    def valueForPathChanged(path: TreePath, newValue: Object) {}
    def isLeaf(node: Object): Boolean = isLeafBean(node.asInstanceOf[Bean])
    def getChildCount(parent: Object): Int = getBeanChildCount(parent.asInstanceOf[Bean])
    def getChild(parent: Object, index: Int) = getChildBean(parent.asInstanceOf[Bean], index)
    def getRoot = value
  }

  private def asChildBean(p: Property[_]): Bean = {
      // TODO: Maybe just check the type of the property
    val value = p.get
    // Don't include beans that already have editors.
    if (value != null && !p.hasEditor && classOf[Bean].isInstance(value)) value.asInstanceOf[Bean]
    else null
  }

  def isLeafBean(bean: Bean): Boolean = {
    getBeanChildCount(bean) <= 0
  }

  def getBeanChildCount(bean: Bean): Int = {
    var children = 0
    bean.properties.values foreach {p =>
      if (asChildBean(p) != null) {
        // Bean property
        children += 1
      }
    }
    children
  }

  def indexOfChildBeanInBean(parent: Bean, child: Bean): Int = {
    var i = 0
    var childIndex: Int = -1
    // TODO: More efficient loop
    parent.properties.values foreach {p =>
      val c = asChildBean(p)
      if (c != null) {
        if (child == c) childIndex = i
        i += 1
      }
    }
    childIndex
  }

  def getChildBean(parent: Bean, childIndex: Int): Bean= {
    var i = 0
    var child: Bean = null
    // TODO: More efficient loop
    parent.properties.values foreach {p =>
      val c = asChildBean(p)
      if (c != null) {
        if (i == childIndex) {
          child = c
        }
        i += 1
      }
    }
    child
  }

}