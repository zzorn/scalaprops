package org.scalaprops.ui.editors

import org.scalaprops.ui.Editor

/**
 * 
 */
class BoolEditor extends Editor[Boolean] {
  protected def onExternalValueChange(oldValue: Boolean, newValue: Boolean) = null

  protected def onInit(initialValue: Boolean, name: String) = null
}