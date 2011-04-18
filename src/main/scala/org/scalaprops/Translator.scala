package org.scalaprops

trait Translator[T] extends ((T) => T) {

  /**
   * Changes or filters the input value in some way.
   */
  def apply(inputValue: T): T
}

