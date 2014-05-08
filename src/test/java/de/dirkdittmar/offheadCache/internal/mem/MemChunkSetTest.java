package de.dirkdittmar.offheadCache.internal.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.dirkdittmar.offheapCache.internal.mem.MemChunk;
import de.dirkdittmar.offheapCache.internal.mem.MemChunkSet;

public class MemChunkSetTest {

	@Test(expected = IllegalArgumentException.class)
	public void createTest_tooSmall() {
		new MemChunkSet(0);
	}

	@Test
	public void createTest() {
		final MemChunkSet set = new MemChunkSet(10);
		assertEquals(1, set.size());
	}

	@Test
	public void removeTest_start() {
		final MemChunkSet set = new MemChunkSet(10);

		final MemChunk toRemove1 = new MemChunk(0, 4);
		set.remove(toRemove1);
		List<MemChunk> chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(5, 9), chunks.get(0));

		// remove again
		set.remove(toRemove1);
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(5, 9), chunks.get(0));
	}

	@Test
	public void removeTest_end() {
		final MemChunkSet set = new MemChunkSet(10);

		final MemChunk toRemove1 = new MemChunk(5, 9);
		set.remove(toRemove1);
		List<MemChunk> chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 4), chunks.get(0));

		// remove again
		set.remove(toRemove1);
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 4), chunks.get(0));
	}

	@Test
	public void removeTest_split() {
		final MemChunkSet set = new MemChunkSet(10);

		set.remove(new MemChunk(4, 7));
		assertEquals(2, set.size());
		final List<MemChunk> chunks = set.chunks();
		assertEquals(new MemChunk(0, 3), chunks.get(0));
		assertEquals(new MemChunk(8, 9), chunks.get(1));
	}

	@Test
	public void removeTest_toEmpty() {
		final MemChunkSet set = new MemChunkSet(10);

		set.remove(new MemChunk(8, 9));
		List<MemChunk> chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 7), chunks.get(0));

		set.remove(new MemChunk(0, 3));
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(4, 7), chunks.get(0));

		set.remove(new MemChunk(4, 7));
		chunks = set.chunks();
		assertEquals(0, chunks.size());
	}

	/**
	 * <pre>
	 * [0,1],[4,5],[8,9]
	 * </pre>
	 */
	@Test
	public void addTest1() {
		// setup
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(2, 3));
		set.remove(new MemChunk(6, 7));
		List<MemChunk> chunks = set.chunks();
		assertEquals(3, chunks.size());
		assertEquals(new MemChunk(0, 1), chunks.get(0));
		assertEquals(new MemChunk(4, 5), chunks.get(1));
		assertEquals(new MemChunk(8, 9), chunks.get(2));

		// fill the gaps
		set.add(new MemChunk(2, 3));
		chunks = set.chunks();
		assertEquals(2, chunks.size());
		assertEquals(new MemChunk(0, 5), chunks.get(0));
		assertEquals(new MemChunk(8, 9), chunks.get(1));

		set.add(new MemChunk(6, 7));
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 9), chunks.get(0));
	}

	/**
	 * <pre>
	 * [0,1],[4,5],[8,9]
	 * </pre>
	 */
	@Test
	public void addTest2() {
		// setup
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(2, 3));
		set.remove(new MemChunk(6, 7));
		List<MemChunk> chunks = set.chunks();
		assertEquals(3, chunks.size());
		assertEquals(new MemChunk(0, 1), chunks.get(0));
		assertEquals(new MemChunk(4, 5), chunks.get(1));
		assertEquals(new MemChunk(8, 9), chunks.get(2));

		// fill the gaps
		set.add(new MemChunk(6, 7));
		chunks = set.chunks();
		assertEquals(2, chunks.size());
		assertEquals(new MemChunk(0, 1), chunks.get(0));
		assertEquals(new MemChunk(4, 9), chunks.get(1));

		set.add(new MemChunk(2, 3));
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 9), chunks.get(0));
	}

	@Test
	public void addTest_first() {
		// setup
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(0, 3));
		List<MemChunk> chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(4, 9), chunks.get(0));

		// Test
		set.add(new MemChunk(0, 3));
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 9), chunks.get(0));
	}

	@Test
	public void addTest_last() {
		// setup
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(5, 9));
		List<MemChunk> chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 4), chunks.get(0));

		// Test
		set.add(new MemChunk(5, 9));
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 9), chunks.get(0));
	}

	@Test
	public void addTest_empty() {
		// setup
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(0, 9));
		List<MemChunk> chunks = set.chunks();
		assertEquals(0, chunks.size());

		// Test
		set.add(new MemChunk(5, 6));
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(5, 6), chunks.get(0));

	}

	@Test
	public void addTest_empty2() {
		// setup
		final MemChunkSet set = new MemChunkSet(20);
		set.remove(new MemChunk(0, 19));
		List<MemChunk> chunks = set.chunks();
		assertEquals(0, chunks.size());

		// Test1
		set.add(new MemChunk(5, 6));
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(5, 6), chunks.get(0));

		// Test1
		set.add(new MemChunk(11, 16));
		chunks = set.chunks();
		assertEquals(2, chunks.size());
		assertEquals(new MemChunk(5, 6), chunks.get(0));
		assertEquals(new MemChunk(11, 16), chunks.get(1));

	}

	@Test
	public void testClear() {
		// setup
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(0, 9));
		List<MemChunk> chunks = set.chunks();
		assertEquals(0, chunks.size());

		set.clear();
		chunks = set.chunks();
		assertEquals(1, chunks.size());
		assertEquals(new MemChunk(0, 9), chunks.get(0));
	}

	@Test
	public void testEmpty() {
		// setup
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(0, 9));
		final List<MemChunk> chunks = set.chunks();
		assertEquals(0, chunks.size());
		assertTrue(set.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOutOfRangeRemove1() {
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(0, 11));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOutOfRangeRemove2() {
		final MemChunkSet set = new MemChunkSet(10);
		set.remove(new MemChunk(1, 11));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOutOfRangeAdd1() {
		final MemChunkSet set = new MemChunkSet(10);
		set.add(new MemChunk(0, 11));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOutOfRangeAdd2() {
		final MemChunkSet set = new MemChunkSet(10);
		set.add(new MemChunk(1, 11));
	}

}
