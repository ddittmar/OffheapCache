package de.dirkdittmar.offheapCache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.dirkdittmar.offheapCache.internal.InternalOffheapMap;
import de.dirkdittmar.offheapCache.internal.Procedure;

public class OffheapByteMap<K> implements CompactableMap<K, byte[]> {

	private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);

	private final InternalOffheapMap<K> map;

	public OffheapByteMap(final int size) {
		this.map = new InternalOffheapMap<K>(size);
	}

	@Override
	public int size() {
		return doWithReadLock(new Procedure<Integer>() {

			@Override
			public Integer call() {
				return map.size();
			}
		});
	}

	@Override
	public boolean isEmpty() {
		return doWithReadLock(new Procedure<Boolean>() {

			@Override
			public Boolean call() {
				return map.isEmpty();
			}
		});
	}

	@Override
	public boolean containsKey(final Object key) {
		return doWithReadLock(new Procedure<Boolean>() {

			@Override
			public Boolean call() {
				return map.containsKey(key);
			}
		});
	}

	@Override
	public boolean containsValue(final Object value) {
		return doWithReadLock(new Procedure<Boolean>() {

			@Override
			public Boolean call() {
				return map.containsValue(value);
			}
		});
	}

	@Override
	public byte[] get(final Object key) {
		return doWithReadLock(new Procedure<byte[]>() {

			@Override
			public byte[] call() {
				return map.get(key);
			}
		});
	}

	@Override
	public byte[] put(final K key, final byte[] value) {
		return doWithWriteLock(new Procedure<byte[]>() {

			@Override
			public byte[] call() {
				return map.put(key, value);
			}
		});
	}

	@Override
	public byte[] remove(final Object key) {
		return doWithWriteLock(new Procedure<byte[]>() {

			@Override
			public byte[] call() {
				return map.remove(key);
			}
		});
	}

	@Override
	public void putAll(final Map<? extends K, ? extends byte[]> m) {
		doWithWriteLock(new Procedure<Void>() {
			@Override
			public Void call() {
				map.putAll(m);
				return null;
			}
		});
	}

	@Override
	public void clear() {
		doWithWriteLock(new Procedure<Void>() {

			@Override
			public Void call() {
				map.clear();
				return null;
			}
		});
	}

	@Override
	public Set<K> keySet() {
		return doWithReadLock(new Procedure<Set<K>>() {

			@Override
			public Set<K> call() {
				return map.keySet();
			}
		});
	}

	/**
	 * This would load all values into the heap area. That's not what this class
	 * is good for. So this is not supported.
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Collection<byte[]> values() {
		throw new UnsupportedOperationException();
	}

	/**
	 * This would load all values into the heap area. That's not what this class
	 * is good for. So this is not supported.
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Set<java.util.Map.Entry<K, byte[]>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] putIfAbsent(final K key, final byte[] value) {
		return doWithWriteLock(new Procedure<byte[]>() {

			@Override
			public byte[] call() {
				return map.putIfAbsent(key, value);
			}
		});
	}

	@Override
	public boolean remove(final Object key, final Object value) {
		return doWithWriteLock(new Procedure<Boolean>() {

			@Override
			public Boolean call() {
				return map.remove(key, value);
			}
		});
	}

	@Override
	public boolean replace(final K key, final byte[] oldValue,
			final byte[] newValue) {
		return doWithWriteLock(new Procedure<Boolean>() {

			@Override
			public Boolean call() {
				return map.replace(key, oldValue, newValue);
			}
		});
	}

	@Override
	public byte[] replace(final K key, final byte[] value) {
		return doWithWriteLock(new Procedure<byte[]>() {

			@Override
			public byte[] call() {
				return map.replace(key, value);
			}
		});
	}

	@Override
	public void compact() {
		doWithWriteLock(new Procedure<Void>() {

			@Override
			public Void call() {
				map.compact();
				return null;
			}
		});
	}

	private <T> T doWithReadLock(final Procedure<T> proc) {
		final Lock readLock = rwLock.readLock();
		readLock.lock();
		try {
			return proc.call();
		} finally {
			readLock.unlock();
		}
	}

	private <T> T doWithWriteLock(final Procedure<T> proc) {
		final Lock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			return proc.call();
		} finally {
			writeLock.unlock();
		}
	}

}
