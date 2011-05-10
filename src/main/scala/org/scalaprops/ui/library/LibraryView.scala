package org.scalaprops.ui.library

import javax.swing.tree.{TreePath, TreeModel}
import net.miginfocom.swing.MigLayout
import javax.swing.event.{TreeSelectionEvent, TreeSelectionListener, TreeModelEvent, TreeModelListener}
import javax.swing._
import java.awt.{Rectangle, FlowLayout, Dimension}
import java.awt.Container._
import javax.swing.JComponent._
import java.awt.Component._
import org.scalaprops.library.{LibraryListener, Library}
import org.scalaprops.Bean
import org.scalaprops.ui.util.SwingUtils
import java.awt.event.{MouseEvent, MouseAdapter, ComponentAdapter}

/**
 *
 */
class LibraryView(library: Library) extends JPanel(new MigLayout()) {

  private var categoryListeners: List[TreeModelListener] = Nil
  private var viewListeners: List[LibraryViewListener] = Nil

  val tree: JTree = createTree()

  def addLibraryViewListener(listener: LibraryViewListener) {viewListeners ::= listener}
  def removeLibraryViewListener(listener: LibraryViewListener) {viewListeners = viewListeners.filterNot(_ == listener)}

  private def createTree(): JTree = {
    val tree = new JTree(new TreeModel(){
      def addTreeModelListener(l: TreeModelListener) { categoryListeners ::= l }
      def removeTreeModelListener(l: TreeModelListener) { categoryListeners = categoryListeners filterNot ( _ == l)}
      def getIndexOfChild(parent: Object, child: Object): Int =parent.asInstanceOf[Library].indexOf(child.asInstanceOf[Library])
      def valueForPathChanged(path: TreePath, newValue: Object) {}
      def isLeaf(node: Object) = if (classOf[Library].isInstance(node)) node.asInstanceOf[Library].isLeafCategory else false
      def getChildCount(parent: Object) = parent.asInstanceOf[Library].subcategoriesCount
      def getChild(parent: Object, index: Int) = parent.asInstanceOf[Library].subCategoryAt(index)
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
            if (classOf[Library].isInstance(selected)) viewListeners.foreach(_.onCategorySelected(selected.asInstanceOf[Library]))
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
            if (classOf[Library].isInstance(selected)) viewListeners.foreach(_.onCategoryContext(selected.asInstanceOf[Library]))
            if (classOf[Bean].isInstance(selected)) viewListeners.foreach(_.onBeanContext(selected.asInstanceOf[Bean]))
          }
        }
      }
    })

    // Listen to changes to lib
    library.addLibraryListener(new LibraryListener {

      def onCategoryRemoved(parentLibrary: Library, category: Library) {notifyTreeChanged()}
      def onCategoryAdded(parentLibrary: Library, category: Library) {notifyTreeChanged()}
      def onBeanRemoved(category: Library, bean: Bean) {}
      def onBeanAdded(category: Library, bean: Bean) {}
    })

    tree
  }

  private def notifyTreeChanged() {
    val path: Array[AnyRef] = Array(library.root)
    categoryListeners foreach {_.treeStructureChanged(new TreeModelEvent(null, path)) }
  }


}