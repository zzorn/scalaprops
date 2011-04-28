package org.scalaprops.ui.editors

import org.scalaprops.ui.Editor

/**
 * 
 */
class StringEditor extends Editor[String] {
  protected def onExternalValueChange(oldValue: String, newValue: String) = null

  protected def onInit(initialValue: String, name: String) = null
}