package org.scalaprops.example

import org.scalaprops.Bean
import org.scalaprops.ui.editors.Slider
import javax.swing.{JComponent, JPanel, JFrame}
import net.miginfocom.swing.MigLayout
import java.awt.{Color, Dimension, BorderLayout}

/**
 * Demonstrates user interface creation for beans.
 */
object UiExample {

  class Furby extends Bean {
    val name = p('name, "Purr")
    val specialPower = p('power, 'Lighting)
    val lives = p('lives, 8)
    val activated = p('activated, false)
    val color = p('color, Color.RED)
    val awesomness = p('awesomness, 4.5).editor(Slider(0.0, 5.0)).onValueChange({(o,n)=>  println("Value changed to " + n)})
  }

  def main(args: Array[ String ])
  {
    val foo = new Furby()

    // Create several editors for the same bean to test multiple views and value change propagation.
    val p = new JPanel(new MigLayout())
    p.add(foo.createEditor)
    p.add(foo.createEditor)
    p.add(foo.createEditor)

    createFrame("Bean Editor Test", p)
  }

  /**
   * Does the boilerplate of just showing a test frame.
   */
  private def createFrame(title: String, content: JComponent) {
    val panel = new JPanel(new BorderLayout)
    panel.add(content, BorderLayout.CENTER)

    val frame = new JFrame(title)
    frame.setPreferredSize(new Dimension(800, 600))
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.getContentPane.add(panel)
    frame.pack
    frame.setVisible(true)
  }
}