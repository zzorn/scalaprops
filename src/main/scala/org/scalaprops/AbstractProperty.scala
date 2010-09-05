package org.scalaprops

/**
 * A value of some type that can be modified.
 */
trait AbstractProperty[T] {

  def get: T
  def set(newValue: T)

  final def apply(): T = get
  final def := (newValue: T) = set(newValue)

}