package org.scalaprops.example

import org.scalaprops._

/**
 * Usage example from readme.txt
 */
object UsageExample {
  def main(args: Array[String]) {

    // Extend org.scalaprops.Bean to get property(...) utility functions and some introspection support.
    class Ball extends Bean {

      val name     = property('name, "beachball")
      val x        = property('x, 0.0) onChange redraw
      val y        = property('y, 0.0) onChange redraw
      val color    = property('color, "ff8800") require(_.size == 6)
      val radius   = property('r, 10) translate(v => if (v < 1) 1 else v)
      val diameter = property('diam, 0) bind(radius, r => r * 2)

      // property(...) can also be shortened to just p(...)

      def redraw() { /* .... */ }
    }

    val ball = new Ball()

    // Accessing properties
    println( ball.color() )

    // Changing properties
    ball.x     := 10.0
    ball.color := "88ffaa"

    // List names and values of properties
    ball.properties foreach { nameAndProperty => println(nameAndProperty._1.name + " = " + nameAndProperty._2.get) }

  }
  
}
