package org.scalaprops.utils

/**
 * 
 */

object ClassUtils {
  def nativeTypeToWrappedType(kind: Class[_]): Class[_] = {
    if (kind == classOf[Int]) classOf[java.lang.Integer]
    else if (kind == classOf[Short]) classOf[java.lang.Short]
    else if (kind == classOf[Long]) classOf[java.lang.Long]
    else if (kind == classOf[Float]) classOf[java.lang.Float]
    else if (kind == classOf[Double]) classOf[java.lang.Double]
    else if (kind == classOf[Boolean]) classOf[java.lang.Boolean]
    else kind
  }
}