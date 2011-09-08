package org.scalaprops.library

import javax.swing.tree.TreePath
import java.io.File
import org.scalaprops.parser.{ParseError, BeanParser}
import java.util.logging.Logger
import java.util.ArrayList
import collection.JavaConversions._
import org.scalaprops.serialization.Serializers
import org.scalaprops.{BeanFactory, Bean}

/**
 * A library containing beans organized into hierarchical categories.
 */
// TODO: Make library a bean with a list property with the child categories and child beans, and a special flag indicating to render it as a library category?
class Category extends Bean {

  // Use java array lists to get fast access by index
//  private val _subCategories: ArrayList[Category] = new ArrayList()
//  private var _components: ArrayList[Bean] = new ArrayList()
  private var listeners: List[CategoryListener] = Nil
  private var _parent: Category = null

  def children: List[AnyRef] = properties.values.toList

  def entries: List[_] = properties.values.map(_.value).filter(!_.isInstanceOf[Category]).toList
  def categories: List[Category] = properties.values.map(_.value).filter(_.isInstanceOf[Category]).toList.asInstanceOf[List[Category]]

  def parent: Category = _parent

  def root: Category = if (parent != null) parent.root else this

  def name

  def addBean(bean: Bean) {
    require(bean != null)
    require(! _components.contains(bean))

    _components.add(bean)

    notifyListeners(l => l.onBeanAdded(this, bean))
  }

  def removeBean(bean: Bean) {
    if (bean != null && _components.contains(bean)) {
      _components.remove(bean)
      notifyListeners(l => l.onBeanRemoved(this, bean))
    }
  }

  def addCategory(name: String): Category = {
    require(name != null, "Name should not be null")
    require(!_subCategories.exists(c => c.name == name), "A category with the same name already exists")

    val subLibrary: Category = new Category(name)

    addCategory(subLibrary)

    subLibrary
  }

  def addCategory(category: Category) {
    require(category != null, "Category should not be null")
    require(!_subCategories.contains(category), "The category was already added")

    // Remove from previous parent
    if (category.parent != null) category.parent.removeCategory(category)

    category._parent = this
    _subCategories.add(category)

    notifyListeners(l => l.onCategoryAdded(this, category))
  }

  def removeCategory(category: Category) {
    if (category != null && _subCategories.contains(category)) {
      _subCategories.remove(category)
      category._parent = null
      notifyListeners(l => l.onCategoryRemoved(this, category))
    }
  }

  def isLeafCategory = _subCategories.isEmpty
  def subcategoriesCount: Int = _subCategories.size
  def indexOf(subCategory: Category): Int = _subCategories.indexOf(subCategory)
  def subCategoryAt(index: Int): Category = if (index < 0 || index >= subcategoriesCount) null else _subCategories(index)

  override def toString = name


  def addLibraryListener(l: CategoryListener) {listeners ::= l}
  def removeLibraryListener(l: CategoryListener) {listeners = listeners filterNot(_ == l)}

  private def notifyListeners(op: CategoryListener => Unit) {
    listeners foreach (l => op(l) )

    // Notify parent listeners also
    parent.notifyListeners(op)
  }

  // TODO: Implement save
}





object Category {

  private val log = Logger.getLogger(Category.getClass.getName)

  // TODO: Implement load from json file / stream

  /**
   * Loads library stored under the specified root directory.
   */
  def load(nameForLibrary: String,
           rootDirectory: File,
           filePattern: (File) => Boolean = {f => f.getName.endsWith(".json")},
           directoryPattern: (File) => Boolean = {f => true},
           parser: BeanParser = Bean.defaultParser,
           beanFactory: BeanFactory = Bean.defaultBeanFactory,
           serializers: Serializers = Bean.defaultSerializers): Category = {

    def loadDirectory(dir: File, category: Category) {
      dir.listFiles foreach {f: File =>
        if (f.isFile && filePattern(f)) {
          // Load bean
          try {
            val bean = Bean.loadFromFile(f, parser, beanFactory, serializers)
            category.addBean(bean)
          } catch {
            case e: ParseError => log.warning("Could not load file '" + f.getName + "' into library, error when parsing it: "+ e.getMessage)
          }
        }
        else if (f.isDirectory && directoryPattern(f)) {
          // Create subcategory
          val subCategory = category.addCategory(f.getName)
          loadDirectory(f, subCategory)
        }
      }
    }

    val library = new Category(nameForLibrary)
    loadDirectory(rootDirectory, library)
    library
  }

}

