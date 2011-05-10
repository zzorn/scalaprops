package org.scalaprops.ui.library

import org.scalaprops.library.Library
import org.scalaprops.Bean

/**
 * A listener that is notified when things are done in the library view.
 */
trait LibraryViewListener {

  def onCategorySelected(category: Library)
  def onBeanSelected(bean: Bean)

  /** Called when the context menu is invoked for a category (e.g. by right click)*/
  def onCategoryContext(category: Library)

  /** Called when the context menu is invoked for a bean (e.g. by right click)*/
  def onBeanContext(bean: Bean)

  // TODO: Implement drag & drop support
  //def onCategoryDragStart(category: Category)
  //def onBeanDragStart(bean: Bean)
  
}