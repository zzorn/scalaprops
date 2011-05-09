package org.scalaprops.library

import java.util.ArrayList
import collection.JavaConversions._
import org.scalaprops.Bean

/**
 * A category of beans.
 */
class Category(val name: String, _library: Library = null) {

  // Use java array lists to get fast access by index
  private val _subCategories: ArrayList[Category] = new ArrayList()
  private var _components: ArrayList[Bean] = new ArrayList()
  private var _parent: Category = null



  def components: List[Bean] = _components.toList
  def subCategories: List[Category] = _subCategories.toList
  def parentCategory = _parent

  def library: Library = if (_library != null) _library else if (_parent != null) _parent.library else throw new IllegalStateException("No library defined for category")

  def addBean(bean: Bean) {
    require(bean != null)
    require(!_components.contains(bean))

    _components.add(bean)

    library.notifyListeners(l => l.onBeanAdded(this, bean))
  }

  def removeBean(bean: Bean) {
    if (bean != null && _components.contains(bean)) {
      _components.remove(bean)
    }

    library.notifyListeners(l => l.onBeanRemoved(this, bean))
  }

  def addCategory(name: String): Category = {
    require(name != null)
    require(!_subCategories.exists(c => c.name == name), "A category with the same name already exists")
    val category: Category = new Category(name)
    addCategory(category)
    category
  }

  def addCategory(category: Category) {
    require(category != null)
    require(!_subCategories.contains(category))

    _subCategories.add(category)
    category._parent = this

    library.notifyListeners(l => l.onCategoryAdded(category))
  }

  def removeCategory(category: Category) {
    if (category != null && _components.contains(category)) {
      _subCategories.remove(category)
      category._parent = null
    }

    library.notifyListeners(l => l.onCategoryRemoved(category))
  }

  def isLeafCategory = _subCategories.isEmpty
  def subcategoriesCount: Int = _subCategories.size
  def indexOf(subCategory: Category): Int = _subCategories.indexOf(subCategory)
  def subCategoryAt(index: Int): Category = if (index < 0 || index >= subcategoriesCount) null else _subCategories(index)

  override def toString = name
}