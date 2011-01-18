package org.scalaprops

/**
 * Can be used to create beans of registered types.
 */
class BeanFactory {
  type BeanConstructor = () => _ <: Bean
  type BeanCreator = Symbol => _ <: Option[Bean]

  private var beanConstructors: Map[Symbol, BeanConstructor] = Map()
  private val initialBeanCreator: BeanCreator = { (name: Symbol) => beanConstructors.get(name).flatMap(x => Some(x())) }
  private var beanCreators: List[BeanCreator] = List(initialBeanCreator)
  private var defaultBeanConstructor: BeanConstructor = {() => new PropertyBean()}

  def registerBeanType(typeName: Symbol, createInstance: BeanConstructor) = beanConstructors += (typeName -> createInstance)
  def registerBeanTypes(creator: BeanCreator) = beanCreators ::= creator
  def setDefaultBeanType(createInstance: BeanConstructor) = defaultBeanConstructor = createInstance

  def createDefaultBeanInstance(): Bean = defaultBeanConstructor()

  def createBeanInstance(typeName: Symbol, allowFallbackToDefault: Boolean = true): Bean = {
    var bean: Bean = createBeanWithCreator(typeName)
    if (bean == null) {
      if (allowFallbackToDefault) {
        // TODO: Logging
        // println("No bean creator found for bean type " + typeName + ", using default bean type.")
        createDefaultBeanInstance()
      }
      else throw new IllegalStateException("No bean creator found for bean type '"+typeName+"'.")
    }
    else bean
  }

  private def createBeanWithCreator(typeName: Symbol): Bean = {
    var bean: Bean = null
    beanCreators exists {
      bc =>
        bc(typeName) match {
          case None =>
            false
          case Some(b) =>
            bean = b
            bean.setBeanName(typeName)
            true
        }
    }
    bean
  }

}