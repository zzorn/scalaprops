package org.properties

/**
 * Field implementation with most functionality.
 */
class Field[T](val name: Symbol, initialValue: T) extends Property[T] {

  type ChangeListener = Field[T] => Unit
  type Translator = T => T

  private var value: T = initialValue
  private var invariants: List[Invariant[T]] = Nil
  private var listeners: List[ChangeListener] = Nil
  private var translators: List[Translator] = Nil

  def translate(translator: Translator) = { translators = translator :: translators; this }
  
  def require(invariant: Invariant[T]) =  { invariants = invariant :: invariants; this }
  def require(invariant: T => Boolean, message: String = "Incorrect value") = require(ConditionInvariant(invariant, message))

  def onChange(listener: ChangeListener) = { listeners = listener :: listeners; this }
  def onChange(listener: () => Unit) = onChange( p => listener() )

  def get: T = value
  def set(newValue: T) = {
    if (value != newValue) {
      var translatedVal = newValue
      translators foreach (translatedVal = _(translatedVal))
      invariants foreach ( checkInvariant(_, translatedVal) )

      value = translatedVal

      listeners foreach ( _(this) )
    }
  }

  private final def checkInvariant[T](invariant: Invariant[T], value: T) {
    val result = invariant.check(value)
    if (result != null) throw new IllegalArgumentException("Value '"+value+"' not allowed for property "+name.name+", " + result)
  }

}