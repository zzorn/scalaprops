package org.scalaprops.utils

import java.awt.{RenderingHints, Color, Graphics2D, Graphics}

/**
 * Various graphics related utility methods.
 */
object GraphicsUtils {

  /**
   * Converts a Graphics to a Graphics2D
   */
  def toG2( g : Graphics ) : Graphics2D = g.asInstanceOf[Graphics2D]

  /**
   * Changes antialias state to the desired one.  Returns the previous state.
   */
  def setAntialias( g : Graphics, antialiasOn : Boolean  ) : Boolean = {
    val g2 = toG2(g)

    val oldValue = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING ) match {
      case RenderingHints.VALUE_ANTIALIAS_ON => true
      case RenderingHints.VALUE_ANTIALIAS_OFF => false
    }

    val hint = if (antialiasOn)  RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF

    g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, hint )

    oldValue
  }

  def fillRectFunction( g : Graphics,
                      x : Int, y : Int, w : Int, h : Int,
                      colorCalculator : (Float, Float) => Color ) {

    var y0 = y
    while ( y0 < y + h ) {

      var x0 = x
      while ( x0 < x + w ) {

        val rx = (x0 - x).toFloat / w.toFloat
        val ry = (y0 - y).toFloat / h.toFloat

        val color = colorCalculator( rx, ry )

        putPixel( g, x0, y0, color )

        x0 += 1
      }

      y0 += 1
    }

  }


  def putPixel( g : Graphics, x : Int, y : Int, color : Color ) {
    g.setColor( color )
    g.drawRect( x, y, 1 ,1 )
  }


  def antialiased( g : Graphics )( block : => Unit )  {
    antialiased(g, true)(block)
  }

  def antialiased( g : Graphics, antialiasOn : Boolean )( block : => Unit )  {
    val oldAA = setAntialias( g, antialiasOn )
    block
    setAntialias( g, oldAA )
  }


  /**
   * Renders a filled triangle of the specified color with the specified corners.
   */
  def triangle( g: Graphics, color: java.awt.Color, x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float) {
    val g2 = toG2(g)

    val xs = new Array[Int]( 3 )
    val ys = new Array[Int]( 3 )

    xs(0) = x0.toInt
    xs(1) = x1.toInt
    xs(2) = x2.toInt

    ys(0) = y0.toInt
    ys(1) = y1.toInt
    ys(2) = y2.toInt

    g2.setColor(color)
    g2.fillPolygon( xs, ys, 3 )
  }


  def drawRhomb( g: Graphics, color : Color, r : Float, x : Float, y : Float ) {
    triangle( g, color, x-r, y, x, y-r, x+r,y )
    triangle( g, color, x+r, y, x, y+r, x-r,y )
  }

  def drawDiamondIndicator( g: Graphics,
                          borderColor : Color,
                          darkColor : Color,
                          mediumColor : Color,
                          lightColor : Color,
                          size : Float,
                          x : Float, y : Float ) {
    val g2 = toG2(g)

    g2.setColor(darkColor)

    drawRhomb( g, borderColor, size, x, y )
    drawRhomb( g, mediumColor, size-1, x, y )
    drawRhomb( g, darkColor,  size-2, x, y+1 )
    drawRhomb( g, lightColor, size-2, x, y-1 )
    drawRhomb( g, mediumColor, size-3, x, y )
  }

  /**
   *  Paint an indicator for sliders etc.
   */
  def paintIndicator(g2: Graphics2D,
                     width : Int,
                     height: Int,
                     relativePosition: Double,
                     isVertical: Boolean,
                     darkColor : Color,
                     mediumColor : Color,
                     lightColor : Color) {

    def edgeTriangle( color: java.awt.Color, x: Float, y: Float, d1 : Float, d2 :Float, size :Float ) {
      triangle( g2, color,
        x - d1 * size, y - d2 * size,
        x + d2 * size, y + d1 * size,
        x + d1 * size, y + d2 * size)
    }

    def drawTriangles(color1: java.awt.Color, color2: java.awt.Color, x1: Float, y1: Float, x2: Float, y2: Float, d1 : Float, d2 :Float, size :Float ) {
/* Looks better with only one
        edgeTriangle( color1, x1, y1, d1, d2, size )
*/
      edgeTriangle( color2, x2, y2, -d1, -d2, size )
    }

    val w = width.toFloat
    val h = height.toFloat
    val size = (Math.min(w, h) / 3).toInt
    val r = relativePosition.floatValue
    val dx = if (isVertical) 0f else 1f
    val dy = if (isVertical) -1f else 0f
    val x1 = if (isVertical) 0f else r * w
    val x2 = if (isVertical) w - 1f else r * w
    val y1 = if (isVertical) (1f -r) * h else 0f
    val y2 = if (isVertical) (1f -r) * h else h - 1f

    drawTriangles( darkColor, darkColor, x1, y1, x2, y2, dx, dy, size+2 )
    drawTriangles( lightColor, lightColor,  x1, y1, x2, y2, dx, dy, size )
    drawTriangles( mediumColor, mediumColor, x1, y1, x2, y2, dx, dy, size-2 )

  }

}