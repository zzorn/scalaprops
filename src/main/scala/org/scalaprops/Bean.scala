package org.scalaprops

/**
 * Base trait for classes that contain properties.
 * Provides factory method for creating properties, and a query function for returning added properties.
 */
trait Bean {

  private var _properties: Map[Symbol, Property[_]] = Map()

  /**
   * Adds a property to the bean and returns the property,
   * so that translators, validators, or listeners can be easily added to it,
   * and so that it can be assigned to a val for easy access.
   */
  protected def property[T](name: Symbol, initialValue: T): Property[T] = {
    val property = new Property[T](name, initialValue)
    addProperty(property)
    property
  }

  /**
   * Shorthand version of property()
   */
  protected def p[T](name: Symbol, initialValue: T): Property[T] = property(name, initialValue)

  /**
   * Get property with the given name, or throw exception if not found
   */
  def apply[T](propertyName: Symbol): T = _properties(propertyName).get.asInstanceOf[T]

  /**
   * Get value of property as an option.
   */
  def get[T](propertyName: Symbol): Option[T] = _properties.get(propertyName).get.asInstanceOf[Option[T]]
  
  /**
   * Set value for property, throws exception if property doesn't exist.
   */
  def update[T](propertyName: Symbol, value: T) = _properties(propertyName).asInstanceOf[Property[T]] := value

  /**
   * Set value for property, throws exception if property doesn't exist.
   */
  def set[T](propertyName: Symbol, value: T) {
    if (!_properties.contains(propertyName)) throw new IllegalArgumentException("No property named "+propertyName.name+" found")
    _properties(propertyName).asInstanceOf[Property[T]] := value
  }

  /**
   * Returns the properties that have been added to this Bean.
   */
  def properties: Map[Symbol, Property[_]] = _properties

  /**
   * Adds a property to the bean.
   */
  def addProperty(property: Property[_]) = _properties = _properties + (property.name -> property)

}