= Scalaprops =

== About ==

Scalaprops is a simple Scala library for properties oriented programminging.
It provides listener, value translation, and validator support for properties.
It also includes a Bean trait with factory methods for creating properties.


== License ==

New BSD license.


== Usage example ==

// Define Bean that uses properties
class Ball extends Bean {

  val name   = property("beachball")
  val x      = property(0.0) onChange redraw _
  val y      = property(0.0) onChange redraw _
  val color  = property("ff8800") require(_.size() == 6)  
  val radius = property(10) translate(v => if (v < 1) 1 else v)
  // property(...) can also be shortened to just p(...)

  def redraw() { /* .... */ }
}

val ball = new Ball()

// Accessing properties
println( ball.color() )

// Changing properties
ball.x     := 10.0
ball.color := "88ffaa"


== Webpage & contact ==

Web:   http://github.com/zzorn/scalaprops
Git:   git clone git://github.com/zzorn/scalaprops.git
Email: zzorn at iki.fi

