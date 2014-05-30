package de.dirkdittmar.offheapCache.internal;

import static de.dirkdittmar.offheapCache.internal.Preconditions.checkArgument;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.dirkdittmar.offheapCache.CompactableMap;
import de.dirkdittmar.offheapCache.NotEnoughMemException;
import de.dirkdittmar.offheapCache.internal.mem.MemChunk;
import de.dirkdittmar.offheapCache.internal.mem.MemChunkSet;

public class InternalOffheapMap<K> implements CompactableMap<K, byte[]> {

	private final ByteBuffer buffer;

	private final MemChunkSet freeChunks;

	private final Map<K, MemChunk> entries = new HashMap<>();

	public InternalOffheapMap(final int size) {
		checkArgument(size > 0, "size > 0");
		buffer = ByteBuffer.allocateDirect(size);
		freeChunks = new MemChunkSet(size);
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return entries.containsKey(key);
	}

	@Override
	public byte[] get(final Object key) {
		checkArgument(key != null, "key != null");
		byte[] result = null;
		final MemChunk range = entries.get(key);
		if (range != null) {
			result = read(range);
		}
		return result;
	}

	private byte[] read(final MemChunk range) {
		byte[] result;
		result = new byte[range.size()];
		final ByteBuffer readBuffer = buffer.asReadOnlyBuffer();
		readBuffer.clear();
		readBuffer.position(range.loIdx);
		readBuffer.get(result);
		return result;
	}

	@Override
	public byte[] put(final K key, final byte[] value) {
		checkArgument(key != null, "key != null");
		checkArgument(value != null, "value != null");

		if (value.length > buffer.capacity()) {
			throw new NotEnoughMemException("Not enough memory in this cache");
		}

		byte[] result = null;
		final MemChunk range = entries.get(key);
		if (range != null) {
			result = delete(range);
		}
		write(key, value);
		return result;

	}

	private void write(final K key, final byte[] value) {
		MemChunk freeRange = findRangeWithEnoughSpace(value.length);
		if (freeRange == null) {
			compact();
			freeRange = findRangeWithEnoughSpace(value.length);
		}
		if (freeRange == null) {
			throw new NotEnoughMemException("Not enough memory in this cache");
		}

		final Integer lower = freeRange.loIdx;
		final Integer upper = lower + value.length - 1;
		buffer.position(lower);
		buffer.put(value);
		final MemChunk writtenRange = new MemChunk(lower, upper);
		freeChunks.remove(writtenRange);
		entries.put(key, writtenRange);
	}

	private byte[] delete(final MemChunk chunk) {
		byte[] result;
		result = read(chunk);
		freeChunks.add(chunk); // free the chunk
		return result;
	}

	private MemChunk findRangeWithEnoughSpace(final int minSpace) {
		for (final MemChunk freeRange : freeChunks) {
			if (freeRange.size() >= minSpace) {
				return freeRange;
			}
		}
		return null;
	}

	@Override
	public void compact() {
		// more than one free chunk?
		if (freeChunks.size() > 1) {
			final Deque<MemChunk> freeChunkStack = freeChunks.chunks();
			final Deque<ReverseEntry<K>> valuesStack = sortedEntries();

			int moveBy = 0;
			while (!freeChunkStack.isEmpty()) {
				final MemChunk first = freeChunkStack.pop();
				moveBy += first.size();
				if (!freeChunkStack.isEmpty()) {
					final MemChunk second = freeChunkStack.peek();

					while (!valuesStack.isEmpty()) {
						final ReverseEntry<K> entry = valuesStack.pop();
						if (entry.value.isRightOf(first)) {
							if (entry.value.isLeftOf(second)) {
								final byte[] data = read(entry.value);
								final Integer lower = entry.value.loIdx
										- moveBy;
								final Integer upper = lower + data.length - 1;
								buffer.position(lower);
								buffer.put(data);
								final MemChunk writtenRange = new MemChunk(
										lower, upper);
								entries.put(entry.key, writtenRange);
							} else {
								valuesStack.push(entry);
								break;
							}
						}
					}
				}
			}

			freeChunks.clear();
			for (final MemChunk entryChunk : entries.values()) {
				freeChunks.remove(entryChunk);
			}
		}
	}

	private Deque<ReverseEntry<K>> sortedEntries() {
		final LinkedList<ReverseEntry<K>> result = new LinkedList<>();
		for (final Map.Entry<K, MemChunk> entry : entries.entrySet()) {
			result.add(new ReverseEntry<K>(entry.getValue(), entry.getKey()));
		}
		Collections.sort(result, new Comparator<ReverseEntry<K>>() {

			@Override
			public int compare(final ReverseEntry<K> o1,
					final ReverseEntry<K> o2) {
				return o1.value.compareTo(o2.value);
			}

		});
		return result;
	}

	@Override
	public byte[] remove(final Object key) {
		byte[] result = null;
		final MemChunk range = entries.remove(key);
		if (range != null) {
			result = delete(range);
		}
		return result;
	}

	@Override
	public void putAll(final Map<? extends K, ? extends byte[]> map) {
		for (final Map.Entry<? extends K, ? extends byte[]> entry : map
				.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		freeChunks.clear();
		entries.clear();
	}

	@Override
	public Set<K> keySet() {
		return new HashSet<K>(entries.keySet());
	}

	@Override
	public byte[] putIfAbsent(final K key, final byte[] value) {
		if (!containsKey(key)) {
			return put(key, value);
		} else {
			return get(key);
		}
	}

	@Override
	public boolean remove(final Object key, final Object value) {
		checkArgument(value instanceof byte[], "value instanceof byte[]");
		final byte[] byteArr = (byte[]) value;

		if (containsKey(key) && Arrays.equals(get(key), byteArr)) {
			remove(key);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean replace(final K key, final byte[] oldValue,
			final byte[] newValue) {
		if (containsKey(key) && Arrays.equals(get(key), oldValue)) {
			put(key, newValue);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public byte[] replace(final K key, final byte[] value) {
		if (containsKey(key)) {
			return put(key, value);
		} else {
			return null;
		}
	}

	@Override
	public boolean containsValue(final Object value) {
		checkArgument(value instanceof byte[], "value instanceof byte[]");

		final byte[] val = (byte[]) value;
		for (final MemChunk chunk : entries.values()) {
			final byte[] arr = read(chunk);
			if (Arrays.equals(arr, val)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This would load all values into the heap area. That's not what this class
	 * is good for. So this is not supported.
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Collection<byte[]> values() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * This would load all values into the heap area. That's not what this class
	 * is good for. So this is not supported.
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Set<Map.Entry<K, byte[]>> entrySet()
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	private static class ReverseEntry<K> {

		public final MemChunk value;

		public final K key;

		public ReverseEntry(final MemChunk value, final K key) {
			this.value = value;
			this.key = key;
		}

	}

}
