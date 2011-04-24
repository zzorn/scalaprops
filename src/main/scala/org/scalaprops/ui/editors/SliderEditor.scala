package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}
import javax.swing.JSlider
import org.scalaprops.ui.util.NamedPanel
import javax.swing.event.{ChangeEvent, ChangeListener}

/**
 * 
 */
class SliderEditor[T](start: T, end: T, c: Class[T]) extends NamedPanel with Editor[T] {
  private val MAX_UI_VALUE = 1000
  private var slider: JSlider = null
  private val startD: Double = toDouble(start)
  private val endD: Double = toDouble(end)

  protected def onValueChange(oldValue: T, newValue: T) {
    valueToUi(newValue)
  }

  protected def onInit(initialValue: T, name: String) {
    title = name
    slider = new JSlider(0, MAX_UI_VALUE)
    slider.addChangeListener(new ChangeListener {
      def stateChanged(e: ChangeEvent) {
        // TODO: Notify all the time or only when finished?
        onEditorChange(uiToValue)
      }
    })
    add(slider, "dock south")
    valueToUi(initialValue)
  }

  private def valueToUi(v: T) {
    val d = toDouble(v)
    val sliderPos = if (endD == startD) 0
                    else (MAX_UI_VALUE * (d - startD) / (endD - startD)).intValue
    slider.setValue(sliderPos)
  }

  private def uiToValue: T = {
    val v = slider.getValue.toDouble / MAX_UI_VALUE
    toT(startD + (endD - startD) * v)
  }

  private def toDouble(v: T): Double = {
    if (c.isAssignableFrom(classOf[Byte])) v.asInstanceOf[Byte].doubleValue
    else if (c.isAssignableFrom(classOf[Short])) v.asInstanceOf[Short].doubleValue
    else if (c.isAssignableFrom(classOf[Int])) v.asInstanceOf[Int].doubleValue
    else if (c.isAssignableFrom(classOf[Long])) v.asInstanceOf[Long].doubleValue
    else if (c.isAssignableFrom(classOf[Float])) v.asInstanceOf[Float].doubleValue
    else if (c.isAssignableFrom(classOf[Double])) v.asInstanceOf[Double].doubleValue
    else if (v == end) 1.0 else 0.0
  }

  private def toT(v: Double): T = {
    if (c.isAssignableFrom(classOf[Byte])) v.byteValue.asInstanceOf[T]
    else if (c.isAssignableFrom(classOf[Short])) v.shortValue.asInstanceOf[T]
    else if (c.isAssignableFrom(classOf[Int])) v.intValue.asInstanceOf[T]
    else if (c.isAssignableFrom(classOf[Long])) v.longValue.asInstanceOf[T]
    else if (c.isAssignableFrom(classOf[Float])) v.floatValue.asInstanceOf[T]
    else if (c.isAssignableFrom(classOf[Double])) v.doubleValue.asInstanceOf[T]
    else if (v <= 0.5) start else end
  }
}

case class Slider[T](start: T, end: T)(implicit m: Manifest[T]) extends EditorFactory[T] {
  protected def createEditorInstance = new SliderEditor(start, end, m.erasure.asInstanceOf[Class[T]])
}

