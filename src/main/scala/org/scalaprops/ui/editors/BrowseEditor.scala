package org.scalaprops.ui.editors

import org.scalaprops.ui.Editor

/**
 * Select value by browsing from a list or tree of alternatives.
 */
class BrowseEditor extends Editor[AnyRef] {
  protected def onExternalValueChange(oldValue: AnyRef, newValue: AnyRef) = null

  protected def onInit(initialValue: AnyRef, name: String) = null
}