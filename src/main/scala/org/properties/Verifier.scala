package org.properties

/**
 * 
 */
trait Verifier[T] extends Property[T] {

  var verifiers: List[(T => Boolean, String)] = Nil

  def addVerifier(check: T => Boolean, description: String) = verifiers = (check, description) :: verifiers

  override def set(newValue: T) {
    verifiers foreach { p =>
      if (!p._1(newValue)) throw new IllegalArgumentException("Value "+newValue+" not allowed, " + p._2)
    }

    super.set(newValue)
  }
}