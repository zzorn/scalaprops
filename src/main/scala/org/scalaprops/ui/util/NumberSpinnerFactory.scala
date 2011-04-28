package org.scalaprops.ui.util

import javax.swing.{JSpinner, SpinnerNumberModel}
import java.util.Locale
import java.text.{ParsePosition, DecimalFormatSymbols, DecimalFormat}
import javax.swing.text.{DefaultFormatterFactory, NumberFormatter}
import java.awt.event.{MouseWheelEvent, MouseWheelListener}
import org.scalaprops.utils.ClassUtils

/**
 * Factory object used to hide nasty swing incantations.
 */
// TODO: Replace the whole spinner implementation at some point, the swing one requires jumping through too many broken hoops.
object NumberSpinnerFactory extends JSpinner {

  def createNumberSpinner(kind: Class[_], defaultValue: Number, step: Number, fractionalNumbers: Boolean, min: Number = null, max: Number = null): JSpinner = {
    val numberModel = new SpinnerNumberModel(defaultValue,
                                             min.asInstanceOf[Comparable[Number]],
                                             max.asInstanceOf[Comparable[Number]],
                                             step)
    val spinner: JSpinner = new JSpinner(numberModel)

    // TODO: Do not accept character input.  That is hard to implement, document filter on number editor
    //       text field didn't work, and implementing a custom format that disallows characters but allows
    //       invalid inputs with regards to decimal dot etc. is a pain too.

    // NOTE: Java has a bug, where if you delete characters from the right and have a pattern like #0.0#,
    //       it goes in sequence 1.01_ -> 1.0_ -> 10_.0 -> 1_.0 -> _0.0, where _ is cursor pos

    val pattern = if (fractionalNumbers) "#0.####" else "#"

    val format = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ROOT)) {
      override def parse(text: String, pos: ParsePosition): Number = {
        // Ignore trailing decimal dot
        if (text.endsWith(".")) super.parse(text + "0", pos)
        else super.parse(text, pos)
      }
    }

    //format.setDecimalSeparatorAlwaysShown(fractionalNumbers)
    //format.setParseIntegerOnly(false)
    //format.setGroupingUsed(false)
    //format.setMinimumFractionDigits(0)

    val formatter: NumberFormatter = new NumberFormatter(format)
    formatter.setCommitsOnValidEdit(true)
    formatter.setMaximum(max.asInstanceOf[Comparable[Number]])
    formatter.setMinimum(min.asInstanceOf[Comparable[Number]])
    //formatter.setAllowsInvalid(false)

    spinner.getEditor.asInstanceOf[JSpinner.NumberEditor].getTextField.setFormatterFactory(new DefaultFormatterFactory(formatter))

    // Move spinner with mouse wheel (for some reason swing doesn't do it by default)
    spinner.addMouseWheelListener(new MouseWheelListener {
      def mouseWheelMoved(e: MouseWheelEvent) {
        val rotation = -e.getWheelRotation
        val stepSize = step.doubleValue
        val oldValue = ClassUtils.tToDouble(spinner.getValue, kind.asInstanceOf[Class[Any]])
        val newValue = oldValue + stepSize * rotation
        
        if (min != null && newValue <= min.doubleValue) spinner.setValue(min)
        else if (max != null && newValue >= max.doubleValue) spinner.setValue(max)
        else spinner.setValue( ClassUtils.doubleToT(newValue, kind) )
      }
    })

    spinner
  }

}