package org.scalaprops.library

import org.scalaprops.Bean

trait CategoryListener {
  def onCategoryAdded(parentLibrary: Category, category: Category)
  def onCategoryRemoved(parentLibrary: Category, category: Category)
  def onBeanAdded(library: Category, bean: Bean)
  def onBeanRemoved(library: Category, bean: Bean)
}