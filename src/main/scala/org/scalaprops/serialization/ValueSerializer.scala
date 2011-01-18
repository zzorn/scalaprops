package org.scalaprops.serialization

/**
 * Something that can serialize and unserialize a value of some bean property.
 */
case class ValueSerializer(kind: Class[_], serialize: (AnyRef) => String, deserialize: (String) => AnyRef)