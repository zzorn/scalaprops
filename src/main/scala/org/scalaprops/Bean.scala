package org.scalaprops

import collection.immutable.ListMap

/**
 * Base trait for classes that contain properties.
 * Provides factory method for creating properties, and a query function for returning added properties.
 */
trait Bean {

  private var _properties: Map[Symbol, Property[_]] = ListMap()
  private var _beanName: Symbol = Symbol(getClass.getSimpleName)

  def beanName: Symbol = _beanName

  def beanName_=(name: Symbol) { _beanName = name }

  /**
   * Adds a property to the bean and returns the property,
   * so that translators, validators, or listeners can be easily added to it,
   * and so that it can be assigned to a val for easy access.
   */
  protected def property[T](name: Symbol, initialValue: T)(implicit m: Manifest[T]): Property[T] = {
    val property = new Property[T](name, initialValue)
    addProperty(property)
    property
  }

  /**
   * Shorthand version of property()
   */
  protected def p[T](name: Symbol, initialValue: T)(implicit m: Manifest[T]): Property[T] = property(name, initialValue)

  /**
   * Get property with the given name, or throw exception if not found
   */
  def apply[T](propertyName: Symbol): T = _properties(propertyName).get.asInstanceOf[T]

  def update[T](propertyName: Symbol, value: T) { set(propertyName, value) }

  /**
   * True if a property with the specified name is present.
   */
  def contains(name: Symbol): Boolean = properties.contains(name)

  /**
   * Get value of property as an option.
   */
  def get[T](propertyName: Symbol): Option[T] = _properties.get(propertyName) match {
    case None => None
    case Some(p: Property[T]) => Some(p.get)
  }

  /**
   * Get value of property, or the specified default value if not found.
   */
  def get[T](propertyName: Symbol, defaultValue: T): T = get(propertyName).getOrElse(defaultValue)

  /**
   * Set value for property, throws exception if property doesn't exist.
   */
  def set[T](propertyName: Symbol, value: T) {
    if (!_properties.contains(propertyName)) throw new IllegalArgumentException("Can not set property "+propertyName.name+" for "+beanName+", the property has not beed added.")
    _properties(propertyName).asInstanceOf[Property[T]] := value
  }

  /**
   * Returns the properties that have been added to this Bean.
   */
  def properties: Map[Symbol, Property[_]] = _properties

  /**
   * Adds or updates the value of the property.
   */
  def put[T](name: Symbol, value: T)(implicit m: Manifest[T]) = {
    if (contains(name)) set(name, value)
    else addProperty(name, value)
  }

  /**
   * Adds a property to the bean.
   */
  def addProperty[T](name: Symbol, value: T)(implicit m: Manifest[T]): Property[T] = addProperty(new Property[T](name, value))

  /**
   * Adds a property to the bean.
   */
  def addProperty[T](property: Property[T]): Property[T] = {
    _properties = _properties + (property.name -> property)
    property
  }

  /**
   * The properties as a map.
   */
  def toMap: Map[Symbol, AnyRef] = _properties map (e => (e._1, e._2.get.asInstanceOf[AnyRef]))

  /**
   * Set values from the specified map.
   */
  def setFromMap(values: Map[Symbol, AnyRef]) = values foreach (e => set(e._1, e._2))

  /**
   * Add or update values from the specified map.
   */
  def putFromMap(values: Map[Symbol, AnyRef]) = values foreach (e => put(e._1, e._2))

  /**
   * Add or reset values from the specified map.
   */
  def addFromMap(values: Map[Symbol, AnyRef]) = values foreach (e => addProperty(e._1, e._2))

  /**
   * Calls updateFromBound for all properties in this bean, updating the property values
   * from their bound values.  Only needs to be called if automatic updates in bindings are not used.
   */
  def updateBoundValues() {
    _properties.values foreach (p => p.updateFromBound())
  }

  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("{\n")
    properties.values foreach (p => {
      sb.append(p.name).append(": ").append(p.value).append("\n")
    })
    sb.append("}\n")
    sb.toString
  }

}