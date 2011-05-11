package org.scalaprops

/**
 * A value of some type that can be modified.
 */
trait AbstractProperty[T] {

  /**
   * Name of property.
   */
  def name: Symbol

  /**
   * returns the current value of the property.
   */
  def get: T

  /**
   * Set value of property.
   */
  def set(newValue: T)

  /**
   * The current value of the property.  An alias for get.
   */
  final def value: T = get

  /**
   * The current value of the property.  An alias for get.
   */
  final def apply(): T = get


  /**
   * Set value of property.
   */
  final def := (newValue: T) { set(newValue) }

}
