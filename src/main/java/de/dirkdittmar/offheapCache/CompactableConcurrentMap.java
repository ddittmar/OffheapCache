package de.dirkdittmar.offheapCache;

import java.util.concurrent.ConcurrentMap;

public interface CompactableConcurrentMap<K, V> extends ConcurrentMap<K, V> {

	void compact();

}
