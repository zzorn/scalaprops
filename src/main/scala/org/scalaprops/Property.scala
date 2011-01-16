package org.scalaprops

import utils.ClassUtils

/**
 * Property implementation with listener, validation, and translation support.
 */
class Property[T](val name: Symbol, initialValue: T)(implicit val kind: Manifest[T]) extends AbstractProperty[T] {

  type ChangeListener = Property[T] => Unit
  type Translator = T => T
  type Validator = T => String

  private val typeValidator: Validator = {value: T =>
    if (value == null ||
        kind.erasure.isInstance(value) ||
        ClassUtils.nativeTypeToWrappedType(kind.erasure).isInstance(value)) null
    else "Value is not of the correct type, expected "+kind.erasure.getName+", but got "+value.asInstanceOf[AnyRef].getClass().getName+"."
  }

  private var _value: T = initialValue
  private var validators: List[Validator] = List( typeValidator )
  private var listeners: List[ChangeListener] = Nil
  private var translators: List[Translator] = Nil

  private var _boundProperty: Property[T] = null
  private var _boundListener: Property[T] => Unit = null

  /**
   * Add a function that is used to process the value assigned to the property before it is assigned.
   * The result of the function is used as the value for the property.
   */
  def translate(translator: Translator): Property[T] = { translators = translator :: translators; this }

  /**
   * Adds a validator that checks a property value about to be assigned, and returns an error message if it is incorrect,
   * and null if it is ok.  Validators are checked after all translators have been applied to the new value.
   */
  def requireValidator(validator: Validator): Property[T] =  { validators = validator :: validators; this }

  /**
   * Adds a validator that checks a property value about to be assigned, and returns the specified error message if it is incorrect,
   * and null if it is ok.  Validators are checked after all translators have been applied to the new value.
   */
  def require(invariant: T => Boolean, message: String = "Incorrect value"): Property[T] = requireValidator( (t:T) => if (!invariant(t)) message else null )

  /**
   * Adds a validator that checks that a property value about to be assigned is not null.
   * Validators are checked after all translators have been applied to the new value.
   */
  def requireNotNull: Property[T] = requireValidator( (t:T) => if (t == null) "Null not allowed" else null )

  /**
   * Adds a listener that is called when the property changes.  The listener takes the property as a parameter.
   */
  def onChange(listener: ChangeListener): Property[T] = { listeners = listener :: listeners; this }

  /**
   * Adds a listener function without parameters that is called when the property changes.
   * This type of listener can not be removed with removeListener
   */
  def onChange(listener: () => Unit): Property[T] = onChange( p => listener() )

  /**
   * Removes the specified changeListener.
   */
  def removeListener(listener: ChangeListener): Property[T] = { listeners = listeners.filterNot(_ == listener); this }

  /**
   * Bind this property to listen to the specified other property and immediately
   * translate and copy its value when it is changed.
   *
   * First unbinds from any previous bound property.
   * 
   * If automaticUpdate is true the value of this property is immediately updated if the other property changes.
   * If not, the value of this property is only updated when updateFromBound is called.
   */
  def bind(other: Property[T], translator: T => T = t => t, automaticUpdate: Boolean = true): Property[T] = {
    if (_boundProperty != null) unbind()

    _boundProperty = other
    _boundListener = (p: Property[T]) => set(translator(p.get))
    _boundListener(_boundProperty)
    if (automaticUpdate) _boundProperty.onChange(_boundListener)
    this
  }

  /**
   * Updates the value of this property with that of the property it is bound to.
   * If this property is not bound to any other property it is not changed.
   * Used when bindings are updated manually.
   */
  def updateFromBound() {
    if (_boundProperty != null) {
      _boundListener(_boundProperty)
    }
  }

  /**
   * Unbind this property, stopping any automatic updates of it.
   */
  def unbind() = {
    if (_boundProperty != null) _boundProperty.removeListener(_boundListener)
    _boundProperty = null
    _boundListener = null
  }

  /**
   * The property that this property is bound to, or null if none.
   */
  def boundProperty: Property[T] = _boundProperty

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

  private def bindingListener(p: Property[T]) = set(p.get)

}