package org.scalaprops

trait Validator[T] extends ((T) => ValidationResult) {

  /**
   * Test the value, return Success if the value is acceptable, Failure if it is not allowed for the property.
   */
  def apply(value: T): ValidationResult

}