package org.properties

import scala.math.Ordering
import scala.math.Ordering._

// TODO: Fix ordered thingy or just delete, fast to define on the fly. 

/**
 * 
 */
trait Invariant[T] {
  def check(value: T): String
}

case class ConditionInvariant[T](condition: T => Boolean, message: String) extends Invariant[T] {
  def check(value: T) = if (!condition(value)) message else null
}

case class InRange[T <: Ordered[T]](min: T, max: T) extends Invariant[T] {
  def check(value: T) = if (value < min || value > max) "should be between " + min + " and " + max else null
}

case class Larger[T <: Ordered[T]](min: T) extends Invariant[T] {
  def check(value: T) = if (value <= min) "should be larger than " + min else null
}

case class LargerOrEqual[T <: Ordered[T]](min: T) extends Invariant[T] {
  def check(value:  T) = if (value < min) "should be larger or equal to " + min else null
}

case class Smaller[T <: Ordered[T]](max: T) extends Invariant[T] {
  def check(value: T) = if (value>= max) "should be smaller than " + max else null
}

case class SmallerOrEqual[T <: Ordered[T]](max: T) extends Invariant[T] {
  def check(value: T) = if (value> max) "should be smaller or equal to " + max else null
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
