
package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}
import java.lang.String
import javax.swing.{JSpinner}
import javax.swing.event.{ChangeEvent, ChangeListener}
import org.scalaprops.ui.util.{NumberSpinnerFactory, NamedPanel}

class NumberEditorFactory[T](kind: Class[T], defaultValue: T, step: T, min: T, max: T, fractionalNumbers: Boolean) extends EditorFactory[T] {
  protected def createEditorInstance = new NumberEditor[T](kind, defaultValue, step, min, max, fractionalNumbers)
}

class ByteEditorFactory(step: Byte = 1, min: Byte = Byte.MinValue, max: Byte = Byte.MaxValue)
        extends NumberEditorFactory[Byte](classOf[Byte], 0, step, min, max, false)

class ShortEditorFactory(step: Short = 1, min: Short = Short.MinValue, max: Short = Short.MaxValue)
        extends NumberEditorFactory[Short](classOf[Short], 0, step, min, max, false)

class IntEditorFactory(step: Int = 1, min: Int = Int.MinValue, max: Int = Int.MaxValue)
        extends NumberEditorFactory[Int](classOf[Int], 0, step, min, max, false)

class LongEditorFactory(step: Long = 1, min: Long = Long.MinValue, max: Long = Long.MaxValue)
        extends NumberEditorFactory[Long](classOf[Long], 0, step, min, max, false)

class FloatEditorFactory(step: Float = 1, min: Float = Float.NegativeInfinity, max: Float = Float.PositiveInfinity)
        extends NumberEditorFactory[Float](classOf[Float], 0f, step, min, max, true)

class DoubleEditorFactory(step: Double = 1, min: Double = Double.NegativeInfinity, max: Double = Double.PositiveInfinity)
        extends NumberEditorFactory[Double](classOf[Double], 0.0, step, min, max, true)

/**
 * 
 */
class NumberEditor[T](kind: Class[T], defaultValue:T, step: T, min: T, max: T, fractionalNumbers: Boolean = true) extends NamedPanel with Editor[T] {

  private val numberSpinner: JSpinner = NumberSpinnerFactory.createNumberSpinner(kind,
                                                                                 defaultValue.asInstanceOf[Number],
                                                                                 step.asInstanceOf[Number],
                                                                                 fractionalNumbers,
                                                                                 min.asInstanceOf[Number],
                                                                                 max.asInstanceOf[Number])


  numberSpinner.addChangeListener(new ChangeListener{
    def stateChanged(e: ChangeEvent) {
      onEditorChange(numberSpinner.getValue.asInstanceOf[T])
    }
  })

  add(numberSpinner, "align right, width 100px")

  protected def onExternalValueChange(oldValue: T, newValue: T) {
    numberSpinner.setValue(newValue)
  }

  protected def onInit(initialValue: T, name: String) {
    numberSpinner.setValue(initialValue)
  }

}

