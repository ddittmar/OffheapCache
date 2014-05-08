package de.dirkdittmar.offheapCache;

import java.util.concurrent.ConcurrentMap;

public interface CompactableMap<K, V> extends ConcurrentMap<K, V> {

	void compact();

}
