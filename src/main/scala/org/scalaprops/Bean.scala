package org.scalaprops

import collection.immutable.ListMap
import exporter.{JsonBeanExporter, BeanExporter}
import parser.{JsonBeanParser, BeanParser}
import serialization.{ValueSerializer, StandardSerializers, Serializers}
import ui.editors.{NestedBeanEditor, BeanEditor}
import org.scalaprops.Property
import java.io._
import utils.{ClassUtils, CollectionUtils}
import collection.immutable.Map._
import javax.swing.{AbstractAction, Action, JMenuItem, JPopupMenu}
import java.awt.event.{MouseEvent, MouseListener, MouseAdapter, ActionEvent}

/**
 * Base trait for classes that contain properties.
 * Provides factory method for creating properties, and a query function for returning added properties.
 */
// TODO: Support for editors for lists of beans, where you can add new instances (by pasting, copying existing, or from a library). Also reorder support
// TODO: Support lists of beans in treeview too?  And maps, sets?
// TODO: Add range modifier for numbers, have UI use it automatically, and validators check it
// TODO: Some enum specification that scalaprops supports / understands (to get multiselect editor and serialization support)
// TODO: Handle de-selection of any element in a tree view

// TODO: Add interpolate method
// TODO: Add random variation method? (for genetic algos)
// TODO: Add crossover / mix method (for genetic algos)
trait Bean {

  implicit def propertyToValue[T](prop: Property[T]): T = prop.value

  private var _properties: Map[Symbol, Property[_]] = ListMap()
  private var _beanName: Symbol = Symbol(getClass.getSimpleName)
  private var listeners: List[BeanListener] = Nil
  private var deepListeners: List[BeanListener] = Nil

  private val deepListener: BeanListener = new BeanListener {
    def onPropertyRemoved(bean: Bean, property: Property[_]) {
      deepListeners.filterNot(_ == deepListener) foreach {_.onPropertyRemoved(bean, property)}
    }
    def onPropertyAdded(bean: Bean, property: Property[_]) {
      deepListeners.filterNot(_ == deepListener) foreach {_.onPropertyAdded(bean, property)}
    }
    def onPropertyChanged(bean: Bean, property: Property[_]) {
      deepListeners.filterNot(_ == deepListener) foreach {_.onPropertyChanged(bean, property)}

      if (bean == Bean.this) listeners.filterNot(_ == deepListener) foreach {_.onPropertyChanged(bean, property)}
    }
  }


  def beanName: Symbol = _beanName

  def beanName_=(name: Symbol) { _beanName = name }

  /**
   * Adds a property to the bean and returns the property,
   * so that translators, validators, or listeners can be easily added to it,
   * and so that it can be assigned to a val for easy access.
   */
  protected def property[T](name: Symbol, initialValue: T)(implicit m: Manifest[T]): Property[T] = addProperty(name, initialValue)

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
  def put[T](name: Symbol, value: T)(implicit m: Manifest[T]): Property[T] = {
    if (contains(name)) {
      val property = _properties(name).asInstanceOf[Property[T]]
      property.set(value)
      property
    }
    else addProperty(name, value)
  }

  /**
   * Adds a property to the bean.
   */
  def addProperty[T](name: Symbol, value: T)(implicit m: Manifest[T]): Property[T] = {
    val property = new Property[T](name, value, this, deepListener)

    _properties = _properties + (property.name -> property)

    onPropertyAdded(property)

    property
  }

  /**
   * Removes the specified property.
   */
  def removeProperty[T](property: Property[T]) {removeProperty(property.name)}

  /**
   * Removes the property with the specified name.
   */
  def removeProperty(name: Symbol) {
    if (_properties.contains(name)) {
      val prop = _properties(name)
      _properties -= name

      onPropertyRemoved(prop)

      prop.onRemoved()
    }
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

  /**
   * Adds a listener that is notified when properties are changed, added, or removed from this bean.
   */
  def addListener(listener: BeanListener) {
    require(listener != null, "Listener should not be null")
    listeners ::= listener
  }

  /**
   * Removes a BeanListener.
   */
  def removeListener(listener: BeanListener) {
    listeners = CollectionUtils.removeOne(listener, listeners)
  }

  /**
   * Adds a listener that is notified when properties are changed, added, or removed from
   * this bean or any bean that is a value in a property of this bean.
   */
  def addDeepListener(listener: BeanListener) {
    require(listener != null, "Listener should not be null")
    deepListeners ::= listener
  }

  /**
   * Removes a deep BeanListener.
   */
  def removeDeepListener(listener: BeanListener) {
    deepListeners  = CollectionUtils.removeOne(listener, deepListeners)
  }

  /**
   * Creates a new instance of this type of bean.  Just calls the default parameterless constructor by default.
   * Used when creating copies of the bean.
   * Override if the bean implementation doesn't have any parameterless constructor, or needs some special initialization.
   */
  def createNewInstance[T <: Bean](): T = getClass.newInstance().asInstanceOf[T]

  /**
   * Create a deep copy of this bean - any contained bean property values are also copied.
   */
  // TODO: How to handle references to group parameters?  Specify handling policy as parameter?  Leave placeholders?  Create new parameters? <- sounds good.
  def copyNested[T <: Bean](): T = {
    copy({p: Property[_] =>
      if (classOf[Bean].isInstance(p.value)) p.value.asInstanceOf[Bean].copyNested()
      else p.value
    })
  }

  /**
   * Create a copy of this bean.  By default does a shallow copy.
   *
   * valueFilter is used to get the value of each copied property if specified.
   * it can be used to implement deep copies, see deepCopy.
   */
  // TODO: How to handle references to group parameters?  Specify handling policy as parameter?  Leave placeholders?  Create new parameters? <- sounds good.
  def copy[T <: Bean](valueFilter: (Property[_]) => Any = {_.value}): T = {
    val beanCopy: T = createNewInstance()

    // Copy properties
    _properties.values.foreach( {p =>
      p.copyTo(beanCopy, valueFilter)
    })

    beanCopy
  }


  /**
   * Saves this bean to the specified filename.
   * Uses the provided bean exporter, or a JSON -format exporter by default.
   */
  def saveToFileNamed(outputFileName: String,
           exporter: BeanExporter = Bean.defaultExporter,
           serializers: Serializers = Bean.defaultSerializers) {
    saveToFile(new File(outputFileName), exporter, serializers)
  }

  /**
   * Saves this bean to the specified file.
   * Uses the provided bean exporter, or a JSON -format exporter by default.
   */
  def saveToFile(outputFile: File,
           exporter: BeanExporter = Bean.defaultExporter,
           serializers: Serializers = Bean.defaultSerializers) {
    val outputStream: FileOutputStream = new FileOutputStream(outputFile)
    saveToStream(outputStream, exporter, serializers)
    outputStream.close()
  }

  /**
   * Saves this bean to the specified stream.
   * Uses the provided bean exporter, or a JSON -format exporter by default.
   */
  def saveToStream(outputStream: OutputStream,
           exporter: BeanExporter = Bean.defaultExporter,
           serializers: Serializers = Bean.defaultSerializers) {
    saveToWriter(new OutputStreamWriter(outputStream), exporter, serializers)
  }

  /**
   * Serializes this bean to a string.
   * Uses the provided bean exporter, or a JSON -format exporter by default.
   */
  def saveToString(exporter: BeanExporter = Bean.defaultExporter,
                   serializers: Serializers = Bean.defaultSerializers): String = {
    val stringWriter: StringWriter = new StringWriter()
    saveToWriter(stringWriter, exporter, serializers)
    stringWriter.toString
  }

  /**
   * Saves this bean to the specified writer.
   * Uses the provided bean exporter, or a JSON -format exporter by default.
   */
  def saveToWriter(writer: Writer,
           exporter: BeanExporter = Bean.defaultExporter,
           serializers: Serializers = Bean.defaultSerializers) {
    val bufferedWriter: BufferedWriter = new BufferedWriter(writer)
    exporter.export(this, bufferedWriter, serializers)
    bufferedWriter.flush()
  }

  /**
   * Creates a UI that can be used to edit this bean.
   */
  def createEditor[T <: Bean](): BeanEditor[T] = {
    val editor = new BeanEditor[T]()
    editor.initForBean(this.asInstanceOf[T])
    editor
  }

  /**
   * Creates a UI that can be used to edit this bean,
   * arranged into a tree that makes it easier to navigate child beans if there are many of them.
   */
  def createNestedEditor[T <: Bean](): NestedBeanEditor[T] = {
    val editor = new NestedBeanEditor[T]()
    editor.initForBean(this.asInstanceOf[T])
    editor
  }

  /**
   * Creates a context menu with actions available for this bean.
   */
  def contextMenu(): JPopupMenu = {
    val menu = new JPopupMenu()

    val actions = contextActions

    actions foreach {a =>
      if (a == null) menu.addSeparator()
      else menu.add(new JMenuItem(a))
    }

    menu
  }

  /**
   * A mouse listener that takes care of opening the popup menu for this bean.
   */
  def createContextMenuOpener(): MouseListener = new MouseAdapter {
    private val popup = Bean.this.contextMenu()

    override def mouseReleased(e: MouseEvent) { handleEvent(e) }
    override def mousePressed(e: MouseEvent) { handleEvent(e) }

    private def handleEvent(e: MouseEvent) {
      if (e.isPopupTrigger) {
        popup.show(e.getComponent, e.getX, e.getY)
      }
    }
  }

  protected def contextActions: List[Action] = {
    var actions: List[Action] = Nil

    def addAction(name: String, action: (Bean) => Unit) {
      actions ::= new AbstractAction(name) {
        def actionPerformed(e: ActionEvent) {
          action(Bean.this)
        }
      }

    }

    def addSeparator() {
      actions ::= null
    }

    // Cut
    addAction("Cut", {
      BeanClipboard.copyToClipboard _
      // TODO: Replace this in parent with default placeholder
    } )

    // Copy
    addAction("Copy", BeanClipboard.copyToClipboard _ )

    // TODO: Have containing bean provide some part of the context menu, e.g. the cut, copy, paste
    // That way non-bean types can be supported also, and beans can be replicated in many different parent beans without problems.

    // Paste / replace over
    // Get parent
    // Replace this in parent with clipboard content

    // Paste / replace after
    // Get parent
    // Replace this in parent with clipboard content where default input is replaced with this

    // Delete (replace with what?)
    // Get parent
    // Replace this in parent with default placeholder

    addSeparator()

    // Group
    // Get parent
    // Crate a group from this bean (and all contained / nested beans).  Handle parameters.
    // Replace this in parent with  the group

    // Ungroup
    // if this is a group, replace it in the parent with its content.  Handle parameter references.

    addSeparator()

    // Save
    // Open save dialog

    // Load from
    // Open load dialog
    // Replace this in parent with loaded content.

    addSeparator()

    actions.reverse
  }

  override def toString: String = beanName.name

  /**
   * Prints a debug output of the bean, with the values of all the properties.
   */
  def toDebugString: String = {
    val sb = new StringBuilder()
    sb.append("{\n")
    properties.values foreach (p => {
      val memberString = if(classOf[Bean].isInstance(p.value)) p.value.asInstanceOf[Bean].toDebugString
                         else p.value
      sb.append(p.name).append(": ").append(memberString).append("\n")
    })
    sb.append("}\n")
    sb.toString()
  }

  private def onPropertyAdded(property: Property[_]) {listeners foreach (_.onPropertyAdded(this, property))}
  private def onPropertyRemoved(property: Property[_]) {listeners foreach (_.onPropertyRemoved(this, property))}

}

object Bean {

  /**
   * Factory used to create bean instances.
   */
  var defaultBeanFactory = new BeanFactory()

  /**
   * Serializers used to parse and write property values of various types.
   */
  var defaultSerializers = new StandardSerializers

  /**
   * The exporter to use to serialize beans when saving them, if no other exporter is specified.
   * Uses a JSON format exporter by default.
   */
  var defaultExporter: BeanExporter = JsonBeanExporter

  /**
   * The parser to use to deserialize beans when loading them, if no other parser is specified.
   * Uses a JSON format parser by default.
   */
  var defaultParser: BeanParser = JsonBeanParser

  /** The property name for fields that indicate what kind of bean the object they are in should be deserialized to. */
  // TODO: Move this to serializers class or similar to remove global effect of this variable.
  var typePropertyName = 'beanType

  /**
   * Register a type of bean that will be accepted by default when parsing beans, e.g. when loading them.
   *
   * By default loaded beans will be instances of PropertyBean, but typically you want the values to be loaded
   * back into your own custom bean implementations.  However, only whitelisted classes should be allowed to be instantiated
   * when reading potentially user-defined datafiles, so you'll have to register the types of beans that can be instantiated
   * with this method first, or by passing them in to the corresponding load method.
   */
  def registerAcceptedBeanType[T <: Bean](acceptedBeanType: Class[T]) {
    require(acceptedBeanType != null, "bean type can not be null")

    defaultBeanFactory.registerBeanType(acceptedBeanType)
  }

  /**
   * Register several accepted bean types.
   */
  def registerAcceptedBeanTypes[T <: Bean](acceptedBeanTypes: Seq[Class[T]]) {
    defaultBeanFactory.registerBeanTypes(acceptedBeanTypes)
  }

  /**
   * Register a serializer and deserializer for some specified type.
   */
  def registerSerializer[T <: AnyRef](serialize: T => String, deserialize: String  => T)(implicit kind: Manifest[T]) {
    defaultSerializers.registerSerializer(serialize, deserialize)(kind)
  }

  /**
   * Load the bean stored in the named file.
   * Uses the specified bean parser, or a JSON-format parser by default.
   * Uses the specified BeanFactory if specified, otherwise the default one where bean types can be registered with the registerBeanType -methods.
   * Throws ParseError if there was some problem reading or parsing the input.
   */
  def loadFromFileNamed(inputFileName: String,
           parser: BeanParser = defaultParser,
           beanFactory: BeanFactory = defaultBeanFactory,
           serializers: Serializers = defaultSerializers): Bean = {

    loadFromFile(new File(inputFileName), parser, beanFactory, serializers)
  }

  /**
   * Load the bean stored in the specified file.
   * Uses the specified bean parser, or a JSON-format parser by default.
   * Uses the specified BeanFactory if specified, otherwise the default one where bean types can be registered with the registerBeanType -methods.
   * Throws ParseError if there was some problem reading or parsing the input.
   */
  def loadFromFile(inputFile: File,
           parser: BeanParser = defaultParser,
           beanFactory: BeanFactory = defaultBeanFactory,
           serializers: Serializers = defaultSerializers): Bean = {

    val inputStream: FileInputStream = new FileInputStream(inputFile)
    val bean = loadFromStream(inputStream, inputFile.getName, parser, beanFactory, serializers)
    inputStream.close()
    bean
  }

  /**
   * Load the bean stored in the specified stream.
   * sourceName is a name for the origin of the stream, used in error messages and such.
   * Uses the specified bean parser, or a JSON-format parser by default.
   * Uses the specified BeanFactory if specified, otherwise the default one where bean types can be registered with the registerBeanType -methods.
   * Throws ParseError if there was some problem reading or parsing the input.
   */
  def loadFromStream(inputStream: InputStream,
           sourceName: String,
           parser: BeanParser = defaultParser,
           beanFactory: BeanFactory = defaultBeanFactory,
           serializers: Serializers = defaultSerializers): Bean = {

    loadFromReader(new InputStreamReader(inputStream), sourceName, parser, beanFactory, serializers)
  }

  /**
   * Load the bean stored in the specified string.
   * sourceName is a name for the origin of the reader, used in error messages and such.
   * Uses the specified bean parser, or a JSON-format parser by default.
   * Uses the specified BeanFactory if specified, otherwise the default one where bean types can be registered with the registerBeanType -methods.
   * Throws ParseError if there was some problem reading or parsing the input.
   */
  def loadFromString(text: String,
           sourceName: String,
           parser: BeanParser = defaultParser,
           beanFactory: BeanFactory = defaultBeanFactory,
           serializers: Serializers = defaultSerializers): Bean = {

    loadFromReader(new StringReader(text), sourceName, parser, beanFactory, serializers)
  }

  /**
   * Load the bean stored in the specified reader.
   * sourceName is a name for the origin of the reader, used in error messages and such.
   * Uses the specified bean parser, or a JSON-format parser by default.
   * Uses the specified BeanFactory if specified, otherwise the default one where bean types can be registered with the registerBeanType -methods.
   * Throws ParseError if there was some problem reading or parsing the input.
   */
  def loadFromReader(reader: Reader,
           sourceName: String,
           parser: BeanParser = defaultParser,
           beanFactory: BeanFactory = defaultBeanFactory,
           serializers: Serializers = defaultSerializers): Bean = {

    parser.parse(new BufferedReader(reader), sourceName, beanFactory, serializers)
  }



}

