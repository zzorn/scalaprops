package org.scalaprops.library

import org.scalaprops.Bean

trait LibraryListener {
  def onCategoryAdded(parentLibrary: Library, category: Library)
  def onCategoryRemoved(parentLibrary: Library, category: Library)
  def onBeanAdded(library: Library, bean: Bean)
  def onBeanRemoved(library: Library, bean: Bean)
}