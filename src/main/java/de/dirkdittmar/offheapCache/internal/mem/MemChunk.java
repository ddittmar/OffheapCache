package de.dirkdittmar.offheapCache.internal.mem;

import static de.dirkdittmar.offheapCache.internal.Preconditions.checkArgument;

import java.util.Collection;

public class MemChunk implements Comparable<MemChunk> {

	public final int loIdx;

	public final int hiIdx;

	public MemChunk(final int loIdx, final int hiIdx) {
		checkArgument(loIdx >= 0);
		checkArgument(hiIdx >= 0);
		checkArgument(loIdx <= hiIdx);

		this.loIdx = loIdx;
		this.hiIdx = hiIdx;
	}

	public int size() {
		return hiIdx + 1 - loIdx;
	}

	public boolean intersects(final MemChunk other) {
		return this.contains(other.loIdx) || this.contains(other.hiIdx);
	}

	public boolean contains(final int byteIndex) {
		return loIdx <= byteIndex && hiIdx >= byteIndex;
	}

	public boolean isNeighbor(final MemChunk other) {
		return (!this.intersects(other))
				&& ((this.hiIdx + 1 == other.loIdx) || (other.hiIdx + 1 == this.loIdx));
	}

	public boolean isRightOf(final MemChunk other) {
		return this.loIdx > other.hiIdx;
	}

	public boolean isLeftOf(final MemChunk other) {
		return other.loIdx > this.hiIdx;
	}

	public MemChunk join(final MemChunk other) {
		final int min = Math.min(this.loIdx, other.loIdx);
		final int max = Math.max(this.hiIdx, other.hiIdx);
		return new MemChunk(min, max);
	}

	public void substract(final MemChunk other,
			final Collection<MemChunk> results) {

		if (this.intersects(other) || other.intersects(this)) {

			if (this.loIdx >= other.loIdx && this.hiIdx <= other.hiIdx) {
				// empty result -> nothing to do
				return;
			} else if (this.loIdx < other.loIdx && this.hiIdx > other.hiIdx) {
				// split
				results.add(new MemChunk(this.loIdx, other.loIdx - 1));
				results.add(new MemChunk(other.hiIdx + 1, this.hiIdx));
			} else if (other.loIdx <= this.loIdx && other.hiIdx <= this.hiIdx) {
				// left
				results.add(new MemChunk(other.hiIdx + 1, this.hiIdx));
			} else {
				// right
				results.add(new MemChunk(this.loIdx, other.loIdx - 1));
			}
		} else {
			results.add(this);
		}
	}

	@Override
	public int compareTo(final MemChunk other) {
		final int x = this.loIdx;
		final int y = other.loIdx;
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + loIdx;
		result = prime * result + hiIdx;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MemChunk other = (MemChunk) obj;
		if (loIdx != other.loIdx)
			return false;
		if (hiIdx != other.hiIdx)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + loIdx + ".." + hiIdx + "]";
	}

}
