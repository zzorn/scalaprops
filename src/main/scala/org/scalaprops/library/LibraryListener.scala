package org.scalaprops.library

import org.scalaprops.Bean

trait LibraryListener {
  def onCategoryAdded(category: Category)
  def onCategoryRemoved(category: Category)
  def onBeanAdded(category: Category, bean: Bean)
  def onBeanRemoved(category: Category, bean: Bean)
}