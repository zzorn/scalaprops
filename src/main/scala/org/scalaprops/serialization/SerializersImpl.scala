package org.scalaprops.serialization

class SerializersImpl extends Serializers {

  private var _serializers: Map[Class[_], ValueSerializer] = Map()

  def registerSerializer[T <: AnyRef](serialize: T => String, deserialize: String  => T)(implicit kind: Manifest[T]) {
    _serializers += (kind.erasure -> new ValueSerializer(kind.erasure, serialize.asInstanceOf[AnyRef => String], deserialize))
  }

  def serializers: Map[Class[_], ValueSerializer] = _serializers
}