package org.scalaprops.ui.util

import java.awt.BorderLayout
import javax.swing.{JLabel, JPanel}
import net.miginfocom.swing.MigLayout

/**
 * A panel with a title above.
 */
class TitledPanel(name: String = "", constraints: String = "") extends JPanel(new MigLayout(constraints)) with TitledContainer {

  add(titleLabel, "dock north")

  title = name


}