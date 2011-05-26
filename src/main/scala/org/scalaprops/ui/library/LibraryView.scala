package org.scalaprops.ui.library

import javax.swing.tree.{TreePath, TreeModel}
import net.miginfocom.swing.MigLayout
import javax.swing.event.{TreeSelectionEvent, TreeSelectionListener, TreeModelEvent, TreeModelListener}
import javax.swing._
import java.awt.{Rectangle, FlowLayout, Dimension}
import java.awt.Container._
import javax.swing.JComponent._
import java.awt.Component._
import org.scalaprops.library.{CategoryListener, Category}
import org.scalaprops.Bean
import org.scalaprops.ui.util.SwingUtils
import java.awt.event.{MouseEvent, MouseAdapter, ComponentAdapter}

/**
 *
 */
class LibraryView(library: Category) extends JPanel(new MigLayout()) {

  private var categoryListeners: List[TreeModelListener] = Nil
  private var viewListeners: List[LibraryViewListener] = Nil

  val tree: JTree = createTree()

  def addLibraryViewListener(listener: LibraryViewListener) {viewListeners ::= listener}
  def removeLibraryViewListener(listener: LibraryViewListener) {viewListeners = viewListeners.filterNot(_ == listener)}

  private def createTree(): JTree = {
    val tree = new JTree(new TreeModel(){
      def addTreeModelListener(l: TreeModelListener) { categoryListeners ::= l }
      def removeTreeModelListener(l: TreeModelListener) { categoryListeners = categoryListeners filterNot ( _ == l)}
      def getIndexOfChild(parent: Object, child: Object): Int =parent.asInstanceOf[Category].indexOf(child.asInstanceOf[Category])
      def valueForPathChanged(path: TreePath, newValue: Object) {}
      def isLeaf(node: Object) = if (classOf[Category].isInstance(node)) node.asInstanceOf[Category].isLeafCategory else false
      def getChildCount(parent: Object) = parent.asInstanceOf[Category].subcategoriesCount
      def getChild(parent: Object, index: Int) = parent.asInstanceOf[Category].subCategoryAt(index)
      def getRoot = library
    })

    // Configure the tree
    tree.setPreferredSize(new Dimension(300, 400))
    tree.setRootVisible(false)
    SwingUtils.expandAll(tree)
    tree.setSelectionPath(new TreePath(library))

    // Listen to tree selections
    tree.addTreeSelectionListener(new TreeSelectionListener{
      def valueChanged(e: TreeSelectionEvent) {
        if (e.getNewLeadSelectionPath != null) {
          val selected = e.getNewLeadSelectionPath.getLastPathComponent
          if (selected != null) {
            if (classOf[Category].isInstance(selected)) viewListeners.foreach(_.onCategorySelected(selected.asInstanceOf[Category]))
            if (classOf[Bean].isInstance(selected)) viewListeners.foreach(_.onBeanSelected(selected.asInstanceOf[Bean]))
          }
        }
      }
    })

    // Listen to context menu invocation
    // NOTE: Assumes a right clicked element will get selected.
    tree.addMouseListener(new MouseAdapter {
      override def mouseReleased(e: MouseEvent) {
        if (e.isPopupTrigger && tree.getSelectionPath != null) {
          val selected = tree.getSelectionPath.getLastPathComponent
          if (selected != null) {
            if (classOf[Category].isInstance(selected)) viewListeners.foreach(_.onCategoryContext(selected.asInstanceOf[Category]))
            if (classOf[Bean].isInstance(selected)) viewListeners.foreach(_.onBeanContext(selected.asInstanceOf[Bean]))
          }
        }
      }
    })

    // Listen to changes to lib
    library.addLibraryListener(new CategoryListener {

      def onCategoryRemoved(parentLibrary: Category, category: Category) {notifyTreeChanged()}
      def onCategoryAdded(parentLibrary: Category, category: Category) {notifyTreeChanged()}
      def onBeanRemoved(category: Category, bean: Bean) {}
      def onBeanAdded(category: Category, bean: Bean) {}
    })

    tree
  }

  private def notifyTreeChanged() {
    val path: Array[AnyRef] = Array(library.root)
    categoryListeners foreach {_.treeStructureChanged(new TreeModelEvent(null, path)) }
  }


}