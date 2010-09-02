package org.properties

/**
 * 
 */
trait Bean {

  private var _properties: List[Property[_]] = Nil

  protected def boolField(name: Symbol, value: Boolean = false): Field[Boolean] = field[Boolean](name, value)
  protected def intField(name: Symbol, value: Int = 0): Field[Int] = field[Int](name, value)
  protected def longField(name: Symbol, value: Long = 0): Field[Long] = field[Long](name, value)
  protected def floatField(name: Symbol, value: Float = 0f): Field[Float] = field[Float](name, value)
  protected def doubleField(name: Symbol, value: Double = 0.0): Field[Double] = field[Double](name, value)
  protected def stringField(name: Symbol, value: String = ""): Field[String] = field[String](name, value)
  protected def field[T](name: Symbol, initialValue: T): Field[T] = {
    val field = new Field[T](name, initialValue)
    _properties = field :: _properties
    field
  }

  def properties: List[Property[_]] = _properties

  def addProperty(property: Property[_]) = _properties = property :: _properties

}