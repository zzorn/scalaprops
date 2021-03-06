package org.scalaprops

import javax.swing.JSpinner.DefaultEditor
import org.scalaprops.ui.{DefaultEditorFactory, EditorFactory, Editor}
import utils.{CollectionUtils, ClassUtils}

/**
 * Property implementation with listener, validation, and translation support.
 */
class Property[T](val name: Symbol, initialValue: T, _bean: Bean, deepListener: BeanListener)(implicit val kind: Manifest[T]) extends AbstractProperty[T] {

  private val typeValidator: (T) => ValidationResult = {value: T =>
    if (value == null ||
        kind.erasure.isInstance(value) ||
        ClassUtils.nativeTypeToWrappedType(kind.erasure).isInstance(value)) Success
    else Failure("Value is not of the correct type, expected "+kind.erasure.getName+", " +
                 "but got "+value.asInstanceOf[Object].getClass.getName+".")
  }

  private var validators: List[(T) => ValidationResult] = List( typeValidator )
  private var listeners: List[(T, T) => Unit] = Nil
  private var translators: List[T => T] = Nil

  private var _sourceProperty: Property[T] = null
  private var _boundListener: (T, T) => Unit = null
  private var _editorFactory: (Property[T]) => Editor[T] = null

  private var _value: T = processValue(initialValue)

  private var _desc: String = null

  def getEditorFactory: (Property[T]) => Editor[T] = _editorFactory

  /**
   * Returns the current value of the property.
   */
  def get: T = _value

  /**
   * Sets the value of the property.  If there are any translators they are applied to the value,
   * then any verifiers are run on it. After the value is assigned any listeners are called.
   */
  def set(newValue: T) {
    if (_value != newValue) {

      // Remove any deep listener from old value
      removeDeepListener(_value)

      val oldValue = _value

      // Translate, validate, and deep-listen to new value
      _value = processValue(newValue)

      // Notify listeners
      listeners foreach ( _(oldValue, _value) )
      if (deepListener != null) deepListener.onPropertyChanged(bean, this)
    }
  }

  /**
   * Translate, validate, and deep-listen to a new value.
   * Used both for the initial value and by the setter.
   */
  private def processValue(v: T): T = {
    // Translate value
    var translatedVal = v
    translators foreach (t => translatedVal = t(translatedVal))

    // Validate value
    validators foreach ( checkInvariant(_, translatedVal) )

    // Add deep listener to new value if it is of bean type
    addDeepListener(translatedVal)

    translatedVal
  }


  /**
   * The bean that this property is member of.
   */
  def bean: Bean = _bean

  /**
   * Add a function that is used to process the value assigned to the property before it is assigned.
   * The result of the function is used as the value for the property.
   * You can either define the translator function inline or implement the Translator trait.
   * NOTE: If the same translator is already added, it will not be added another time.
   */
  def translate(translator: T => T): Property[T] = {
    if (!translators.contains(translator)) {
      translators = translator :: translators
    }
    this
  }

  /**
   * Removes the specified translator.
   */
  def removeTranslator(translator: T => T) { translators = translators.filterNot(_ == translator) }

  /**
   * Removes all translators.
   */
  def removeAllTranslators() { translators = Nil }

  /**
   * Adds a validator that checks a property value about to be assigned, and returns a Failure with an
   * error message if it is incorrect, and Success if it is ok.  Validators are checked after all translators
   * have been applied to the new value.
   * You can either define the validator function inline or implement the Validator trait.
   */
  def requireValidator(validator: (T) => ValidationResult): Property[T] =  {
    if (!validators.contains(validator)) {
      validators = validator :: validators
    }
    this
  }

  /**
   * Adds a validator with a specified condition, that should evaluate to true if the value is accepted,
   * and to false if it is not.  Return the specified failure if the value is incorrect.
   * Validators are checked after all translators have been applied to the new value.
   */
  def require(invariant: T => Boolean, error: String = "Incorrect value"): Property[T] = requireValidator( (t:T) => if (!invariant(t)) Failure(error) else Success )

  /**
   * Adds a validator that checks that a property value about to be assigned is not null.
   * Validators are checked after all translators have been applied to the new value.
   */
  def requireNotNull: Property[T] = requireValidator( (t:T) => if (t == null) Failure("Null not allowed") else Success )

  /**
   * Removes the specified requirement.
   */
  def removeRequirement(validator: (T) => ValidationResult) { validators = validators.filterNot(_ == validator) }

  /**
   * Removes all requirements.
   */
  def removeAllRequirements() { validators = Nil }

  /**
   * Adds a listener that is called when the property changes.
   * The listener takes the old value and the new value as parameters.
   * You can either define a listener function inline, or pass in an implementation of the PropertyListener trait.
   */
  def addListener(listener: (T, T) => Unit): Property[T] = { listeners = listener :: listeners; this }

  /**
   * Removes the specified PropertyListeners.
   */
  def removeListener(listener: (T, T) => Unit) {listeners = CollectionUtils.removeOne(listener, listeners)}

  /**
   * Removes all PropertyListeners.
   */
  def removeAllListeners() { listeners = Nil }

  /**
   * Adds a listener that is called when the property changes.
   * The listener takes no parameters.
   */
  def onChange(listener: => Unit): Property[T] = addListener((oldValue: T, newValue:T) => listener )

  /**
   * Adds a listener that is called when the property changes.
   * The listener takes the old value and the new value as parameters.
   * You can either define a listener function inline, or pass in an implementation of the PropertyListener trait.
   *
   * An alias for addListener.
   */
  def onValueChange(listener: (T, T) => Unit): Property[T] = addListener(listener)

  /**
   * Adds a listener that is called when the property changes.
   * The listener takes the parameter, the old value, and the new value as parameters.
   */
  def onPropertyValueChange(listener: (Property[T], T, T) => Unit): Property[T] = addListener((oldValue: T, newValue: T) => listener(this, oldValue, newValue))

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
    if (_sourceProperty != null) unbind()

    _sourceProperty = other
    _boundListener = (oldValue: T, newValue: T) => set(translator(newValue))
    _boundListener(_sourceProperty.get, _sourceProperty.get)
    if (automaticUpdate) _sourceProperty.addListener(_boundListener)
    this
  }

  /**
   * Updates the value of this property with that of the property it is bound to.
   * If this property is not bound to any other property it is not changed.
   * Used when bindings are updated manually.
   */
  def updateFromBound() {
    if (_sourceProperty != null) {
      _boundListener(_sourceProperty.get, _sourceProperty.get)
    }
  }

  /**
   * Unbind this property, stopping any automatic updates of it.
   */
  def unbind() {
    if (_sourceProperty != null) _sourceProperty.removeListener(_boundListener)
    _sourceProperty = null
    _boundListener = null
  }

  /**
   * The property that this property is bound to, or null if none.
   */
  def boundProperty: Property[T] = _sourceProperty

  /**
   * Set the description of this property.
   * Can be used in UI tooltips and the like.
   */
  def describe(description: String) {_desc = description}

  /**
   * Returns a description of this property, or null if none has been given.
   */
  def description: String = _desc

  /**
   * Specifies the editor type to use for this property.
   * Allows more detailed configuration than a default editor.
   */
  def editor(editorFactory: (Property[T]) => Editor[T]): Property[T] = {_editorFactory = editorFactory; this}

  /**
   * True if the property has some user defined editor.
   */
  def hasEditor: Boolean = _editorFactory != null

  /**
   * Creates a new editor UI for this property.
   */
  def createEditor: Editor[T] = {
    if (_editorFactory != null) _editorFactory(this)
    else DefaultEditorFactory.createEditorFor(kind.erasure.asInstanceOf[Class[T]], this)
  }

  /**
   * Copies this property to the specified bean, either adding it if it doesn't exist, or updating the existing instance.
   * Also copies the editor factory, the description, the translators, and the validators.
   * Doesn't copy listeners or bound properties.
   * Returns the copy or the updated existing property.
   * A valueFilter can used to get the value, allowing things like deep copies of beans.
   */
  def copyTo(bean: Bean, valueFilter: (Property[_]) => Any = {_.value}): Property[T] = {

    val copy: Property[T] = bean.put(name, valueFilter(this).asInstanceOf[T] )
    copy.editor(_editorFactory)
    copy.describe(_desc)
    translators.reverse foreach {t => copy.translate(t)}
    validators.reverse foreach {v => copy.requireValidator(v)}
    copy
  }

  /**
   * Called when the property was removed from a bean.
   * Stops listeners etc.
   */
  private[scalaprops] def onRemoved() {
    removeDeepListener(_value)
    unbind()
  }

  private final def checkInvariant(validator: (T) => ValidationResult, value: T) {
    validator(value) match {
      case Success => // All ok
      case Failure(error) =>
        throw new IllegalArgumentException("Value '"+value+"' not allowed for property "+name.name+": " + error)
    }
  }

  private def removeDeepListener(v: T) {
    if (v != null && deepListener != null && v.isInstanceOf[ Bean ]) {
      val beanValue = v.asInstanceOf[ Bean ]
      beanValue.removeDeepListener(deepListener)
    }
  }

  private def addDeepListener(v: T) {
    if (v != null && deepListener != null && classOf[Bean].isInstance(v)) {
      val beanValue = v.asInstanceOf[ Bean ]
      beanValue.addDeepListener(deepListener)
    }
  }


}