package org.scalaprops.ui.util

import javax.swing.JTree

/**
 * 
 */

object SwingUtils {
  /**
   * Expands all nodes of the tree.
   */
  def expandAll(tree: JTree) {
    var row = 0
    while (row < tree.getRowCount) {
        tree.expandRow(row)
        row += 1
    }
  }

  /**
   * Collapses all open nodes of the tree.
   */
  def collapseAll(tree: JTree) {
    var row = tree.getRowCount - 1
    while (row >= 0) {
        tree.collapseRow(row)
        row -= 1
    }
  }


}