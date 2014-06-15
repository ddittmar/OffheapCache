package de.dirkdittmar.offheapCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OffheapMap<K, V> implements CompactableConcurrentMap<K, V> {

	private final ByteConverter<V> converter;
	
	private final CompactableConcurrentMap<K, byte[]> map;
	
	public OffheapMap(final ByteConverter<V> converter, final int size) {
		this(converter, new OffheapByteMap<K>(size));
	}

	/**
	 * For internal use or testing only!
	 */
	OffheapMap(final ByteConverter<V> converter, final CompactableConcurrentMap<K, byte[]> map) {
		this.converter = converter;
		this.map = map;
	}

	@Override
	public V putIfAbsent(final K key, final V value) {
		final byte[] bytes = converter.toBytes(value);
		final byte[] result = map.putIfAbsent(key, bytes);
		return converter.toValue(result);
	}

	@Override
	public boolean remove(final Object key, final Object value) {
		final byte[] bytes = converter.toBytes((V) value);
		return map.remove(key, bytes);
	}

	@Override
	public boolean replace(final K key, final V oldValue, final V newValue) {
		final byte[] oldBytes = converter.toBytes(oldValue);
		final byte[] newBytes = converter.toBytes(newValue);
		return map.replace(key, oldBytes, newBytes);
	}

	@Override
	public V replace(final K key, final V value) {
		final byte[] bytes = converter.toBytes(value);
		final byte[] result = map.replace(key, bytes);
		return converter.toValue(result);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return map.containsValue(converter.toBytes((V) value));
	}

	@Override
	public V get(final Object key) {
		final byte[] result = map.get(key);
		return converter.toValue(result);
	}

	@Override
	public V put(final K key, final V value) {
		final byte[] valueBytes = converter.toBytes(value);
		final byte[] result = map.put(key, valueBytes);
		return converter.toValue(result);
	}

	@Override
	public V remove(final Object key) {
		final byte[] result = map.remove(key);
		return converter.toValue(result);
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		final Map<K, byte[]> tmp = new HashMap<>(m.size());
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			tmp.put(entry.getKey(), converter.toBytes(entry.getValue()));
		}
		map.putAll(tmp);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	/**
	 * This would load all values into the heap area. That's not what this class
	 * is good for. So this is not supported.
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	/**
	 * This would load all values into the heap area. That's not what this class
	 * is good for. So this is not supported.
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void compact() {
		map.compact();
	}
	
}
