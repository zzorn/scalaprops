package org.scalaprops

/**
 * A listener that is invoked when a property changes.
 */
trait PropertyListener[T] extends ((T, T) => Unit) {

  /**
   * @param oldValue the value that the property changes from.
   * @param newValue the value that the property changes to.
   */
  def apply(oldValue: T, newValue: T)

}