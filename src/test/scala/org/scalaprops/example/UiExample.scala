package org.scalaprops.example

import org.scalaprops.Bean
import javax.swing.{JComponent, JPanel, JFrame}
import net.miginfocom.swing.MigLayout
import java.awt.{Color, Dimension, BorderLayout}
import org.scalaprops.ui.editors._

/**
 * Demonstrates user interface creation for beans.
 */
object UiExample {

  class PowerUp extends Bean {
    val name = p('name, "Double Damage")
    val effect = p('effect, 'damagex2)
    val duration = p('duration, 30400)
  }

  class Furby extends Bean {
    val name = p('name, "Purr")
    val specialPower = p('power, 'Lighting).editor(new SelectionEditorFactory[Symbol](List('Thunder, 'Lighting, 'Rain, 'Sunshine), {s => s.name}))
    val desc = p('desc, "furry thingy")
    val isEvil = p('isEvil, true).editor(new BoolEditorFactory())
    val lives = p('lives, 8).editor(new IntEditorFactory(min = 0, max=10, step =2))
    val size = p('size, 0.7f).editor(new FloatEditorFactory(min = 0f))
    val powerUp = p('powerUp, new PowerUp()).editor(new BeanEditorFactory())
    val activated = p('activated, false)
    val color = p('color, Color.RED)
    val awesomness = p('awesomness, 4.5).editor(SliderFactory(0.0, 5.0, restrictNumberFieldMax = false)).onValueChange({(o,n)=>  println("Value changed to " + n)})
  }

  def main(args: Array[ String ])
  {
    val foo: Furby = new Furby()

    // Create several editors for the same bean to test multiple views and value change propagation.
    val p = new JPanel(new MigLayout())
    p.add(foo.createEditor(), "width 100%")
    p.add(foo.createNestedEditor(), "width 100%")

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
    frame.pack()
    frame.setVisible(true)
  }
}