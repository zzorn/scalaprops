package org.scalaprops.utils

/**
 * 
 */
object MathUtils {

  def clampToZeroToOne(a: Double): Double = {
    if (a <= 0.0)
      0.0
    else if (a >= 1.0)
      1.0
    else
      a
  }

}