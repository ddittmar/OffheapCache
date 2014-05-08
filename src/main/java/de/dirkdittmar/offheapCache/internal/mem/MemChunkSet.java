package de.dirkdittmar.offheapCache.internal.mem;

import static de.dirkdittmar.offheapCache.internal.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

public class MemChunkSet implements Iterable<MemChunk> {

	private final MemChunk initialChunk;

	private final SortedSet<MemChunk> freeChunks = new TreeSet<>();

	public MemChunkSet(final int size) {
		checkArgument(size >= 1);

		this.initialChunk = new MemChunk(0, size - 1);
		freeChunks.add(this.initialChunk);
	}

	public int size() {
		return freeChunks.size();
	}

	/**
	 * get all chunks in order
	 */
	public LinkedList<MemChunk> chunks() {
		return new LinkedList<MemChunk>(freeChunks);
	}

	public boolean isEmpty() {
		return freeChunks.isEmpty();
	}

	@Override
	public Iterator<MemChunk> iterator() {
		return freeChunks.iterator();
	}

	public void clear() {
		freeChunks.clear();
		freeChunks.add(initialChunk);
	}

	public void add(final MemChunk chunk) {
		checkOutOfRange(chunk);

		if (freeChunks.isEmpty()) {
			freeChunks.add(chunk);
			return; // shortcut
		}

		// join with all free chunks
		boolean joined = false;
		final Collection<MemChunk> newChunks = new TreeSet<>();
		for (final MemChunk freeChunk : freeChunks) {
			if (freeChunk.isNeighbor(chunk) || freeChunk.intersects(chunk)
					|| chunk.intersects(freeChunk)) {
				newChunks.add(freeChunk.join(chunk));
				joined = true;
			} else {
				newChunks.add(freeChunk);
			}
		}
		if (!joined) {
			newChunks.add(chunk);
		}
		freeChunks.clear();
		freeChunks.addAll(newChunks);

		if (freeChunks.size() > 1) {
			// setup
			final Deque<MemChunk> stack = new LinkedList<>(freeChunks);
			freeChunks.clear();

			// join all neighbors
			while (!stack.isEmpty()) {
				final MemChunk chunk1 = stack.pop();
				if (stack.isEmpty()) {
					// only one element left
					freeChunks.add(chunk1);
				} else {
					final MemChunk chunk2 = stack.pop();
					if (chunk1.isNeighbor(chunk2) || chunk1.intersects(chunk2)
							|| chunk2.intersects(chunk1)) {
						freeChunks.add(chunk1.join(chunk2));
					} else {
						freeChunks.add(chunk1);
						stack.push(chunk2);
					}
				}
			}
		}
	}

	private void checkOutOfRange(final MemChunk chunk) {
		checkArgument(chunk.loIdx >= initialChunk.loIdx
				&& chunk.hiIdx <= initialChunk.hiIdx, "out of range: %s",
				initialChunk);
	}

	public void remove(final MemChunk chunk) {
		checkOutOfRange(chunk);

		final Collection<MemChunk> newChunks = new LinkedList<>();
		for (final MemChunk freeChunk : freeChunks) {
			freeChunk.substract(chunk, newChunks);
		}
		freeChunks.clear();
		freeChunks.addAll(newChunks);
	}

	@Override
	public String toString() {
		return freeChunks.toString();
	}

}
