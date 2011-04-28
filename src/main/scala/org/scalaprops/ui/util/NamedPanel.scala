package org.scalaprops.ui.util

import net.miginfocom.swing.MigLayout
import javax.swing.{JLabel, JPanel}

/**
 * A panel with a name in the left column
 */
class NamedPanel(name: String = "") extends JPanel(new MigLayout()) with TitledContainer {

  add(titleLabel, "pushx 100")
  title = name

}