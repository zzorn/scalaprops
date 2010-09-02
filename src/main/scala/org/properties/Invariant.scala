package org.properties

/**
 * 
 */
trait Invariant[T] {
  def check(value: T): String
}

case class ConditionInvariant[T](condition: T => Boolean, message: String) extends Invariant[T] {
  def check(value: T) = if (!condition(value)) message else null
}

case class InRange[T <: Number](min: T, max: T) extends Invariant[T] {
  def check(value: T) = if (value.doubleValue < min.doubleValue || value.doubleValue > max.doubleValue) "should be between " + min + " and " + max else null
}

case class Larger[T <: Number](min: T) extends Invariant[T] {
  def check(value: T) = if (value.doubleValue <= min.doubleValue) "should be larger than " + min else null
}

case class LargerOrEqual[T <: Number](min: T) extends Invariant[T] {
  def check(value: T) = if (value.doubleValue < min.doubleValue) "should be larger or equal to " + min else null
}

case class Smaller[T <: Number](max: T) extends Invariant[T] {
  def check(value: T) = if (value.doubleValue >= max.doubleValue) "should be smaller than " + max else null
}

case class SmallerOrEqual[T <: Number](max: T) extends Invariant[T] {
  def check(value: T) = if (value.doubleValue > max.doubleValue) "should be smaller or equal to " + max else null
}

case class NotNull[T]() extends Invariant[T] {
  def check(value: T) = if (value == null) "should not be null" else null
}

case class NotIn[T](nonAllowedValues: T *) extends Invariant[T] {
  def check(value: T) = if (nonAllowedValues.contains(value)) "should not be one of " + nonAllowedValues.mkString("[", ", ", "]") else null
}

case class OneOf[T](allowedValues: T *) extends Invariant[T] {
  def check(value: T) = if (!allowedValues.contains(value)) "should be one of " + allowedValues.mkString("[", ", ", "]") else null
}
