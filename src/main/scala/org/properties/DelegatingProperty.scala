package org.properties

/**
 * A property variant that calls a specified delegate method with its new value when changed.
 */
trait DelegatingProperty[T] extends Property[T] {

  var delegate: (T) => Unit = null

  override def set(newValue: T) {
    if (newValue != get) {
      super.set(newValue)

      if (delegate != null) delegate(get)
    }
  }
}