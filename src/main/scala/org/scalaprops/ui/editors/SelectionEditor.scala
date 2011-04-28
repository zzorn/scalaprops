package org.scalaprops.ui.editors

import org.scalaprops.ui.Editor

/**
 * Select value from a drop-down list.
 */
class SelectionEditor extends Editor[AnyRef] {
  protected def onExternalValueChange(oldValue: AnyRef, newValue: AnyRef) = null

  protected def onInit(initialValue: AnyRef, name: String) = null
}