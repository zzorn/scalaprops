package org.scalaprops

/**
 * Clipboard for beans.
 *
 * Does defensive copies of the bean, so changes to a copied bean does not affect the copy on the clipboard,
 * and each paste of a bean creates a unique copy of it.
 *
 * Values expected as parameter inputs to the group can still break though, if not copied within the current group.
 * (May introduce new parameters without default values, or placeholder values)
 */
// TODO: Backlog history etc?
object BeanClipboard {

  private var content: Bean = null

  def copyToClipboard(bean: Bean) {
    if (bean == null) content = null
    else content = bean.copyNested()
  }

  def pasteFromClipboard[T <: Bean](): T = {
    if (content == null) null.asInstanceOf[T]
    else content.copyNested().asInstanceOf[T]
  }

  def clearClipboard() {content = null}

  def hasContent: Boolean = content != null

  def canCopyTo[T](targetType: Class[T]): Boolean = {
    assert(targetType != null, "target type should not be null")

    if (content == null) false
    else targetType.isAssignableFrom(content.getClass)
  }

}