package org.scalaprops.ui.util

import javax.swing.JLabel

/**
 * Base trait for ui component with changeable title.
 */
trait TitledContainer {

  protected val titleLabel: JLabel = new JLabel("")

  def title = titleLabel.getText
  def title_=(title: String) { titleLabel.setText(title) }


}