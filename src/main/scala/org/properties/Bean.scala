package org.properties

/**
 * Base trait for classes that contain properties.
 * Provides factory method for creating properties, and a query function for returning added properties.
 */
trait Bean {

  private var _properties: List[Property[_]] = Nil

  /**
   * Adds a property to the bean and returns the property,
   * so that translators, validators, or listeners can be easily added to it,
   * and so that it can be assigned to a val for easy access.
   */
  protected def property[T](initialValue: T): Property[T] = {
    val property = new Property[T](initialValue)
    addProperty(property)
    property
  }

  /**
   * Returns the properties that have been added to this Bean.
   */
  def properties: List[Property[_]] = _properties

  /**
   * Adds a property to the bean.
   */
  def addProperty(property: Property[_]) = _properties = property :: _properties

}