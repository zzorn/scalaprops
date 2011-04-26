package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}
import org.scalaprops.ui.util.NamedPanel
import javax.swing.event.{ChangeEvent, ChangeListener}
import java.awt.image.BufferedImage
import java.awt.event._
import org.scalaprops.utils.{GraphicsUtils, MathUtils}
import java.awt._
import javax.swing.{BorderFactory, JPanel, JSlider}

/**
 * 
 */
class SliderEditor[T](start: T, end: T, c: Class[T],
                      backgroundPainter: SliderBackgroundPainter = DefaultSliderBackgroundPainter) extends NamedPanel with Editor[T] {

  private var relativePosition: Double = 0.0;
  private val WHEEL_STEP = 0.05f
  private val SIZE = 32
  private val DEFAULT_HEIGHT: Int = 24

  private val blackColor: Color = new Color( 0,0,0)
  private val darkColor: Color = new Color( 0.25f, 0.25f, 0.25f )
  private val mediumColor: Color = new Color( 0.75f, 0.75f, 0.75f)
  private val lightColor: Color = new Color( 1f,1f,1f )

  private var slider: JPanel = new JPanel() {

    private var buffer: BufferedImage = null
    private var oldPos: Double = Double.NaN

    setPreferredSize(new Dimension(SIZE, DEFAULT_HEIGHT))
    setMinimumSize(new Dimension(DEFAULT_HEIGHT,DEFAULT_HEIGHT))
    setMaximumSize(new Dimension(10000, 10000))
    setBorder(BorderFactory.createLineBorder(Color.BLACK, 1))

    addComponentListener(new ComponentAdapter{
      // Clear buffer to force re-draw of background when the component size changes.
      override def componentResized(e: ComponentEvent) {buffer = null;}
    })

    override def paintComponent(g: Graphics) {

      def regenerateBackground() {
        backgroundPainter.paint(buffer.getGraphics.asInstanceOf[Graphics2D], getWidth, getHeight, relativePosition, isVertical)
      }

      // Paint background
      if (backgroundPainter != null && !(getWidth == 0 || getHeight== 0)) {
        if (buffer == null) {
          buffer = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_RGB)
          regenerateBackground()
        }
        else if (oldPos != relativePosition && backgroundPainter.repaintOnValueChange) {
          oldPos = relativePosition
          regenerateBackground()
        }

        // Paint buffered background picture
        g.asInstanceOf[Graphics2D].drawImage(buffer, 0, 0, null)
      }
      else super.paintComponent(g)

      // Paint current location indicator
      GraphicsUtils.paintIndicator(g.asInstanceOf[Graphics2D],
                                   getWidth,
                                   getHeight,
                                   relativePosition,
                                   isVertical,
                                   darkColor,
                                   mediumColor,
                                   lightColor)
    }

  }

  private val startD: Double = toDouble(start)
  private val endD: Double = toDouble(end)

  protected def onValueChange(oldValue: T, newValue: T) {
    valueToUi(newValue)
  }

  protected def onInit(initialValue: T, name: String) {
    title = name

    slider.addMouseListener(mouseUpdateListener)
    slider.addMouseMotionListener(mouseUpdateListener)
    slider.addMouseWheelListener(mouseUpdateListener)

    add(slider, "dock south")
    valueToUi(initialValue)
  }

  private val mouseUpdateListener = new MouseAdapter() {
    override def mousePressed(e: MouseEvent) {updatePosition(e)}
    override def mouseReleased(e: MouseEvent) {updatePosition(e)}
    override def mouseDragged(e: MouseEvent) {updatePosition(e)}
    override def mouseWheelMoved(e: MouseWheelEvent) {
      val amount = e.getWheelRotation
      relativePosition = MathUtils.clampToZeroToOne(relativePosition + WHEEL_STEP * amount)
      slider.repaint()
      onEditorChange(uiToValue)
    }
  }

  private def isVertical = false

  private def updatePosition(e: MouseEvent) {
    val x = e.getX
    val y = e.getY

    if (isVertical) relativePosition = 1.0f - (1.0f * y) / (1.0f * slider.getHeight)
    else            relativePosition = (1.0f * x) / (1.0f * slider.getWidth)

    relativePosition = MathUtils.clampToZeroToOne(relativePosition)

    slider.repaint()
    onEditorChange(uiToValue)
  }

  private def valueToUi(v: T) {
    val d = toDouble(v)
    val r = if (endD == startD) 0.5
                       else (d - startD) / (endD - startD)
    if (r != relativePosition) {
      relativePosition = r
      slider.repaint()
    }
  }

  private def uiToValue: T = {
    toT(startD + (endD - startD) * relativePosition)
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

case class Slider[T](start: T,
                     end: T,
                     backgroundPainter: SliderBackgroundPainter = DefaultSliderBackgroundPainter)(implicit m: Manifest[T]) extends EditorFactory[T] {
  protected def createEditorInstance = new SliderEditor(start, end, m.erasure.asInstanceOf[Class[T]], backgroundPainter)
}

trait SliderBackgroundPainter {
  def repaintOnValueChange: Boolean
  def paint(graphics: Graphics2D, width: Int, height: Int, relativeValue: Double, vertical: Boolean)
}


object DefaultSliderBackgroundPainter
        extends ColoredSliderBackgroundPainter(
          new Color(1f, 1f, 1f),
          new Color(0.3f, 0.6f, 0.6f))

case class ColoredSliderBackgroundPainter(sliderBgColor: Color, sliderColor: Color) extends SliderBackgroundPainter {
  def repaintOnValueChange = true

  def paint(graphics: Graphics2D,
            width: Int,
            height: Int,
            relativeValue: Double,
            vertical: Boolean) {
    graphics.setColor(sliderBgColor)
    graphics.fillRect(0,0,width, height)

    graphics.setColor(sliderColor)
    if (vertical) graphics.fillRect(0, (height * relativeValue).toInt, width, height)
    else          graphics.fillRect(0, 0, (width * relativeValue).toInt, height)
  }
}
