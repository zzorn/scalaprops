package org.scalaprops.utils

/**
 * 
 */
object CollectionUtils {

  /**
   * Removes first instance of element found, or original list if the element is not in it.
   */
  def removeOne[T](element: T, li: List[T]): List[T] = {
     val (left, right) = li.span(_ != element)
     left ::: right.drop(1)
  }

}