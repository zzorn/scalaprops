package org.scalaprops.ui.editors

import org.scalaprops.ui.Editor

/**
 * 
 */
class NumberEditor extends Editor[Number] {
  protected def onValueChange(oldValue: Number, newValue: Number) = null

  protected def onInit(initialValue: Number, name: String) = null
}