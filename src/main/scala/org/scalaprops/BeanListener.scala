package org.scalaprops

/**
 * Notifies listener when properties have been added or removed to/from the bean.
 */
trait BeanListener {
  def onPropertyAdded(bean: Bean, property: Property[_])
  def onPropertyRemoved(bean: Bean, property: Property[_])
}