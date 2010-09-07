package org.scalaprops

/**
 * Property implementation with listener, validation, and translation support.
 */
class Property[T](val name: Symbol, initialValue: T) extends AbstractProperty[T] {

  type ChangeListener = Property[T] => Unit
  type Translator = T => T
  type Validator = T => String

  private var _value: T = initialValue
  private var validators: List[Validator] = Nil
  private var listeners: List[ChangeListener] = Nil
  private var translators: List[Translator] = Nil

  /**
   * Add a function that is used to process the value assigned to the property before it is assigned.
   * The result of the function is used as the value for the property.
   */
  def translate(translator: Translator): Property[T] = { translators = translator :: translators; this }

  /**
   * Adds a validator that checks a property value about to be assigned, and returns an error message if it is incorrect,
   * and null if it is ok.  Validators are checked after all translators have been applied to the new value.
   */
  def validate(validator: Validator): Property[T] =  { validators = validator :: validators; this }

  /**
   * Adds a validator that checks a property value about to be assigned, and returns the specified error message if it is incorrect,
   * and null if it is ok.  Validators are checked after all translators have been applied to the new value.
   */
  def require(invariant: T => Boolean, message: String = "Incorrect value"): Property[T] = validate( (t:T) => if (!invariant(t)) message else null )

  /**
   * Adds a listener that is called when the property changes.  The listener takes the property as a parameter.
   */
  def onChange(listener: ChangeListener): Property[T] = { listeners = listener :: listeners; this }

  /**
   * Adds a listener function without parameters that is called when the property changes.
   */
  def onChange(listener: () => Unit): Property[T] = onChange( p => listener() )

  /**
   * Returns the current value of the property.
   */
  def get: T = _value

  /**
   * Sets the value of the property.  If there are any translators they are applied to the value,
   * then any verifiers are run on it. After the value is assigned any listeners are called.
   */
  def set(newValue: T) = {
    if (_value != newValue) {
      var translatedVal = newValue
      translators foreach (t => translatedVal = t(translatedVal))
      validators foreach ( checkInvariant(_, translatedVal) )

      _value = translatedVal

      listeners foreach ( _(this) )
    }
  }

  private final def checkInvariant(validator: Validator, value: T) {
    val result = validator(value)
    if (result != null) throw new IllegalArgumentException("Value '"+value+"' not allowed for property "+name.name+", " + result)
  }

}