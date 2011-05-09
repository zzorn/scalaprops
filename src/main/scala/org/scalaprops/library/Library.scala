package org.scalaprops.library

import javax.swing.tree.TreePath
import java.io.File
import org.scalaprops.parser.{ParseError, BeanParser}
import java.util.logging.Logger

/**
 * A library containing beans organized into hierarchical categories.
 */
// TODO: Merge library and category classes?
class Library {

  private var listeners: List[LibraryListener] = Nil

  val root: Category = new Category('Library, this)

  def defaultPath: TreePath = new TreePath(Array[AnyRef](root))

  def addLibraryListener(l: LibraryListener) {listeners ::= l}
  def removeLibraryListener(l: LibraryListener) {listeners = listeners filterNot(_ == l)}

  def notifyListeners(op: LibraryListener => Unit) {
    listeners foreach (l => op(l) )
  }
}

object Library {

  private val log = Logger.getLogger(Library.getClass.getName)

  /**
   * Loads library stored under the specified root directory.
   */
  def load(rootDirectory: File,
           parser: BeanParser,
           filePattern: (File) => Boolean = {f => f.getName.endsWith(".json")},
           directoryPattern: (File) => Boolean = {f => true} ): Library = {

    def loadDirectory(dir: File, category: Category) {
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

    val library = new Library()
    loadDirectory(rootDirectory, library.root)
    library
  }

}

