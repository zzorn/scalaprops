package org.scalaprops.serialization

/**
 * Something that can serialize and deserialize values of several types.
 */
trait Serializers {

  def serializers: Map[Class[_], ValueSerializer]

  def serialize(kind: Class[_], value: AnyRef): String = {
    // Use serializer if found, otherwise just use toString.
    serializers.get(kind).map(ss => ss.serialize(value)).getOrElse(value.toString())
  }

  def deserialize(target: Class[_], s: String): AnyRef = {
    // Use serialize if one found, otherwise return the original input unmodified.
    serializers.get(target).map(ss => ss.deserialize(s)).getOrElse(s)
  }

}
