= Scalaprops =

== About ==

Scalaprops is a simple Scala library for properties oriented programminging.
It provides listener, value translation, and validator support for properties.
It also includes a Bean trait with factory methods for creating properties,
and utility classes for saving and loading beans to/from streams.
Now also includes support for creating property editor UI:s for editing the beans.


== License ==

New BSD license.


== Usage example ==

    import org.scalaprops._

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

    // Create swing UI for editing the bean
    val ui: JComponent = ball.createEditor


== Webpage & contact ==

Web:   http://github.com/zzorn/scalaprops
IRC:   zzorn on irc.freenode.net
Email: zzorn at iki.fi

