package org.scalaprops.library

import javax.swing.tree.TreePath
import java.io.File
import org.scalaprops.parser.{ParseError, BeanParser}
import java.util.logging.Logger
import org.scalaprops.Bean
import java.util.ArrayList
import collection.JavaConversions._

/**
 * A library containing beans organized into hierarchical categories.
 */
class Library(val name: String) {

  // Use java array lists to get fast access by index
  private val _subCategories: ArrayList[Library] = new ArrayList()
  private var _components: ArrayList[Bean] = new ArrayList()
  private var listeners: List[LibraryListener] = Nil
  private var _parent: Library = null

  def beans: List[Bean] = _components.toList
  def categories: List[Library] = _subCategories.toList

  def parent: Library = _parent

  def root: Library = if (parent != null) parent.root else this

  def addBean(bean: Bean) {
    require(bean != null)
    require(!_components.contains(bean))

    _components.add(bean)

    notifyListeners(l => l.onBeanAdded(this, bean))
  }

  def removeBean(bean: Bean) {
    if (bean != null && _components.contains(bean)) {
      _components.remove(bean)
      notifyListeners(l => l.onBeanRemoved(this, bean))
    }
  }

  def addCategory(name: String): Library = {
    require(name != null, "Name should not be null")
    require(!_subCategories.exists(c => c.name == name), "A category with the same name already exists")

    val subLibrary: Library = new Library(name)

    addCategory(subLibrary)

    subLibrary
  }

  def addCategory(category: Library) {
    require(category != null, "Category should not be null")
    require(!_subCategories.contains(category), "The category was already added")

    // Remove from previous parent
    if (category.parent != null) category.parent.removeCategory(category)

    category._parent = this
    _subCategories.add(category)

    notifyListeners(l => l.onCategoryAdded(this, category))
  }

  def removeCategory(category: Library) {
    if (category != null && _subCategories.contains(category)) {
      _subCategories.remove(category)
      category._parent = null
      notifyListeners(l => l.onCategoryRemoved(this, category))
    }
  }

  def isLeafCategory = _subCategories.isEmpty
  def subcategoriesCount: Int = _subCategories.size
  def indexOf(subCategory: Library): Int = _subCategories.indexOf(subCategory)
  def subCategoryAt(index: Int): Library = if (index < 0 || index >= subcategoriesCount) null else _subCategories(index)

  override def toString = name


  def addLibraryListener(l: LibraryListener) {listeners ::= l}
  def removeLibraryListener(l: LibraryListener) {listeners = listeners filterNot(_ == l)}

  private def notifyListeners(op: LibraryListener => Unit) {
    listeners foreach (l => op(l) )

    // Notify parent listeners also
    parent.notifyListeners(op)
  }

  // TODO: Implement save
}





object Library {

  private val log = Logger.getLogger(Library.getClass.getName)

  // TODO: Implement load from json file / stream

  /**
   * Loads library stored under the specified root directory.
   */
  def load(nameForLibrary: String,
           rootDirectory: File,
           parser: BeanParser,
           filePattern: (File) => Boolean = {f => f.getName.endsWith(".json")},
           directoryPattern: (File) => Boolean = {f => true} ): Library = {

    def loadDirectory(dir: File, category: Library) {
      dir.listFiles foreach {f: File =>
        if (f.isFile && filePattern(f)) {
          // Load bean
          try {
            val bean = parser.parse(f)
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

    val library = new Library(nameForLibrary)
    loadDirectory(rootDirectory, library)
    library
  }

}

