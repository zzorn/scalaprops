package org.properties

/**
 * 
 */
trait Property[T] {

  /**
   * The name of the property.
   */
  def name: Symbol
  
  def get: T
  def set(newValue: T)

  final def apply(): T = get
  final def := (newValue: T) = set(newValue)

}