package org.properties

/**
 * 
 */
trait Property[T] {

  private var value: T

  def get: T = value
  def set(newValue: T) = value = newValue

  final def apply(): T = get
  final def := (newValue: T) = set(newValue)

}