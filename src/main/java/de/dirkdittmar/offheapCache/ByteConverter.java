package de.dirkdittmar.offheapCache;

public interface ByteConverter<V> {

	byte[] toBytes(V value);

	V toValue(byte[] bytes);

}
