package de.dirkdittmar.offheadCache.internal.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import de.dirkdittmar.offheapCache.internal.mem.MemChunk;

public class MemChunkTest {

	@Test
	public void testCreate() {
		assertEquals(0, new MemChunk(0, 4).loIdx);
		assertEquals(1, new MemChunk(1, 5).loIdx);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreate_loIdx() {
		new MemChunk(-1, 4);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreate_hiIdx() {
		new MemChunk(1, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreate_range() {
		new MemChunk(4, 1);
	}

	@Test
	public void sizeTest() {
		assertEquals(5, new MemChunk(0, 4).size());
		assertEquals(1, new MemChunk(4, 4).size());
	}

	@Test
	public void compareTest() {
		assertEquals(0, new MemChunk(0, 4).compareTo(new MemChunk(0, 1)));
		assertEquals(-1, new MemChunk(0, 4).compareTo(new MemChunk(1, 1)));
		assertEquals(1, new MemChunk(2, 4).compareTo(new MemChunk(1, 1)));
	}

	@Test
	public void containsTest() {
		final MemChunk memChunk = new MemChunk(1, 4);

		assertFalse(memChunk.contains(-1));
		assertFalse(memChunk.contains(0));

		assertTrue(memChunk.contains(1));
		assertTrue(memChunk.contains(2));
		assertTrue(memChunk.contains(3));
		assertTrue(memChunk.contains(4));

		assertFalse(memChunk.contains(5));
		assertFalse(memChunk.contains(6));
	}

	/**
	 * <pre>
	 *   |------------|
	 * |----|
	 * </pre>
	 */
	@Test
	public void intersectsTest1() {
		final MemChunk memChunk = new MemChunk(5, 10);
		final MemChunk other = new MemChunk(0, 5);

		assertTrue(memChunk.intersects(other));
	}

	/**
	 * <pre>
	 * |------------|
	 *            |----|
	 * </pre>
	 */
	@Test
	public void intersectsTest2() {
		final MemChunk memChunk = new MemChunk(5, 10);
		final MemChunk other = new MemChunk(10, 15);

		assertTrue(memChunk.intersects(other));
	}

	/**
	 * <pre>
	 * |------------|
	 *      |----|
	 * </pre>
	 */
	@Test
	public void intersectsTest3() {
		final MemChunk memChunk = new MemChunk(1, 10);
		final MemChunk other = new MemChunk(5, 7);

		assertTrue(memChunk.intersects(other));
	}

	/**
	 * <pre>
	 * |------------|
	 *                |----|
	 * </pre>
	 */
	@Test
	public void intersectsTest_false1() {
		final MemChunk memChunk = new MemChunk(1, 10);
		final MemChunk other = new MemChunk(11, 15);

		assertFalse(memChunk.intersects(other));
	}

	/**
	 * <pre>
	 *        |------------|
	 * |----|
	 * </pre>
	 */
	@Test
	public void intersectsTest_false2() {
		final MemChunk memChunk = new MemChunk(5, 10);
		final MemChunk other = new MemChunk(1, 3);

		assertFalse(memChunk.intersects(other));
	}

	/**
	 * <pre>
	 * |----------|
	 *            |----|
	 * </pre>
	 */
	@Test
	public void neightborTest1() {
		final MemChunk memChunk = new MemChunk(1, 10);
		final MemChunk other = new MemChunk(11, 15);

		assertTrue(memChunk.isNeighbor(other));
	}

	/**
	 * <pre>
	 *      |----------|
	 * |----|
	 * </pre>
	 */
	@Test
	public void neightborTest2() {
		final MemChunk memChunk = new MemChunk(11, 15);
		final MemChunk other = new MemChunk(1, 10);

		assertTrue(memChunk.isNeighbor(other));
	}

	/**
	 * <pre>
	 * |----------|
	 *              |----|
	 * </pre>
	 */
	@Test
	public void neightborTest_false1() {
		final MemChunk memChunk = new MemChunk(1, 10);
		final MemChunk other = new MemChunk(12, 15);

		assertFalse(memChunk.isNeighbor(other));
	}

	/**
	 * <pre>
	 *        |----------|
	 * |----|
	 * </pre>
	 */
	@Test
	public void neightborTest_false2() {
		final MemChunk memChunk = new MemChunk(12, 15);
		final MemChunk other = new MemChunk(1, 10);

		assertFalse(memChunk.isNeighbor(other));
	}

	/**
	 * <pre>
	 *   |------------|
	 * |----|
	 * </pre>
	 */
	@Test
	public void neightborTest_false3() {
		final MemChunk memChunk = new MemChunk(5, 10);
		final MemChunk other = new MemChunk(0, 5);

		assertFalse(memChunk.isNeighbor(other));
	}

	/**
	 * <pre>
	 * |------------|
	 *            |----|
	 * </pre>
	 */
	@Test
	public void neightborTest_false4() {
		final MemChunk memChunk = new MemChunk(5, 10);
		final MemChunk other = new MemChunk(10, 15);

		assertFalse(memChunk.isNeighbor(other));
	}

	/**
	 * <pre>
	 * |------------|
	 *      |----|
	 * </pre>
	 */
	@Test
	public void neightborTest_false5() {
		final MemChunk memChunk = new MemChunk(1, 10);
		final MemChunk other = new MemChunk(5, 7);

		assertFalse(memChunk.isNeighbor(other));
	}

	/**
	 * <pre>
	 * |-----------|
	 * |-----------|
	 * </pre>
	 */
	@Test
	public void substractTest_equal() {
		final MemChunk chunk1 = new MemChunk(5, 10);
		final MemChunk chunk2 = new MemChunk(5, 10);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(0, results.size());
	}

	/**
	 * <pre>
	 *  |-----------|
	 * |-------------|
	 * </pre>
	 */
	@Test
	public void substractTest_bigger() {
		final MemChunk chunk1 = new MemChunk(5, 10);
		final MemChunk chunk2 = new MemChunk(4, 11);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(0, results.size());
	}

	/**
	 * <pre>
	 *   |-----------|
	 * |---|
	 * </pre>
	 */
	@Test
	public void substractTest1() {
		final MemChunk chunk1 = new MemChunk(5, 10);
		final MemChunk chunk2 = new MemChunk(1, 6);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(1, results.size());
		assertEquals(new MemChunk(7, 10), results.get(0));
	}

	/**
	 * <pre>
	 * |-----------|
	 * |---|
	 * </pre>
	 */
	@Test
	public void substractTest11() {
		final MemChunk chunk1 = new MemChunk(5, 10);
		final MemChunk chunk2 = new MemChunk(5, 6);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(1, results.size());
		assertEquals(new MemChunk(7, 10), results.get(0));
	}

	/**
	 * <pre>
	 * |-----------|
	 *           |---|
	 * </pre>
	 */
	@Test
	public void substractTest2() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(5, 10);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(1, results.size());
		assertEquals(new MemChunk(1, 4), results.get(0));
	}

	/**
	 * <pre>
	 * |-----------|
	 *         |---|
	 * </pre>
	 */
	@Test
	public void substractTest21() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(5, 6);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(1, results.size());
		assertEquals(new MemChunk(1, 4), results.get(0));
	}

	/**
	 * <pre>
	 * |-----------|
	 *       |---|
	 * </pre>
	 */
	@Test
	public void substractTest_split() {
		final MemChunk chunk1 = new MemChunk(0, 10);
		final MemChunk chunk2 = new MemChunk(5, 6);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(2, results.size());
		assertEquals(new MemChunk(0, 4), results.get(0));
		assertEquals(new MemChunk(7, 10), results.get(1));
	}

	/**
	 * <pre>
	 *       |-----------|
	 * |---|
	 * </pre>
	 */
	@Test
	public void substractTest_nothingToDo1() {
		final MemChunk chunk1 = new MemChunk(7, 17);
		final MemChunk chunk2 = new MemChunk(1, 6);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(1, results.size());
		assertEquals(chunk1, results.get(0));
	}

	/**
	 * <pre>
	 * |-----------|
	 *               |---|
	 * </pre>
	 */
	@Test
	public void substractTest_nothingToDo2() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(7, 17);

		final List<MemChunk> results = new LinkedList<>();
		chunk1.substract(chunk2, results);

		assertEquals(1, results.size());
		assertEquals(chunk1, results.get(0));
	}

	/**
	 * <pre>
	 * |-----------|
	 *             |---|
	 * </pre>
	 */
	@Test
	public void join1() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(7, 10);

		final MemChunk joined = chunk1.join(chunk2);

		assertEquals(new MemChunk(1, 10), joined);
	}

	/**
	 * <pre>
	 *     |-----------|
	 * |---|
	 * </pre>
	 */
	@Test
	public void join2() {
		final MemChunk chunk1 = new MemChunk(7, 10);
		final MemChunk chunk2 = new MemChunk(1, 6);

		final MemChunk joined = chunk1.join(chunk2);

		assertEquals(new MemChunk(1, 10), joined);
	}

	/**
	 * <pre>
	 *   |-----------|
	 * |---|
	 * </pre>
	 */
	@Test
	public void join3() {
		final MemChunk chunk1 = new MemChunk(4, 10);
		final MemChunk chunk2 = new MemChunk(1, 6);

		final MemChunk joined = chunk1.join(chunk2);

		assertEquals(new MemChunk(1, 10), joined);
	}

	/**
	 * <pre>
	 * |-----------|
	 *           |---|
	 * </pre>
	 */
	@Test
	public void join4() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(4, 10);

		final MemChunk joined = chunk1.join(chunk2);

		assertEquals(new MemChunk(1, 10), joined);
	}

	/**
	 * <pre>
	 * |-----------|
	 *                 |---|
	 * </pre>
	 */
	@Test
	public void join5() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(8, 10);

		final MemChunk joined = chunk1.join(chunk2);

		assertEquals(new MemChunk(1, 10), joined);
	}

	@Test
	public void testEquals() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(1, 6);

		assertEquals(chunk1, chunk2);
		assertTrue(chunk1.equals(chunk2));
		assertEquals(chunk2, chunk1);
		assertTrue(chunk2.equals(chunk1));
	}

	@Test
	public void testNotEquals1() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(8, 10);

		assertNotEquals(chunk1, chunk2);
		assertFalse(chunk1.equals(chunk2));
		assertNotEquals(chunk2, chunk1);
		assertFalse(chunk2.equals(chunk1));
	}

	@Test
	public void testNotEquals2() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(1, 10);

		assertNotEquals(chunk1, chunk2);
		assertFalse(chunk1.equals(chunk2));
		assertNotEquals(chunk2, chunk1);
		assertFalse(chunk2.equals(chunk1));
	}

	@Test
	public void testNotEqualsNull() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = null;

		assertNotEquals(chunk1, chunk2);
		assertFalse(chunk1.equals(chunk2));
	}

	@Test
	public void testNotEqualsOtherClass() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final Object chunk2 = new Object();

		assertNotEquals(chunk1, chunk2);
		assertFalse(chunk1.equals(chunk2));
	}

	@Test
	public void testHashCode() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		final MemChunk chunk2 = new MemChunk(1, 6);
		assertTrue(chunk1.hashCode() == chunk2.hashCode());

		final MemChunk chunk3 = new MemChunk(8, 10);
		assertFalse(chunk1.hashCode() == chunk3.hashCode());
	}

	@Test
	public void testToString() {
		final MemChunk chunk1 = new MemChunk(1, 6);
		assertEquals("[1..6]", chunk1.toString());
		final MemChunk chunk2 = new MemChunk(4, 8);
		assertEquals("[4..8]", chunk2.toString());
	}

	/**
	 * <pre>
	 *   |------------|
	 * |----|
	 * </pre>
	 */
	@Test
	public void leftOrRightTest1() {
		final MemChunk memChunk = new MemChunk(5, 10);
		final MemChunk other = new MemChunk(0, 5);

		assertFalse(memChunk.isLeftOf(other));
		assertFalse(memChunk.isRightOf(other));
	}

	/**
	 * <pre>
	 * |------------|
	 *            |----|
	 * </pre>
	 */
	@Test
	public void leftOrRightTest2() {
		final MemChunk memChunk = new MemChunk(5, 10);
		final MemChunk other = new MemChunk(10, 15);

		assertFalse(memChunk.isLeftOf(other));
		assertFalse(memChunk.isRightOf(other));
	}

	/**
	 * <pre>
	 * |------------|
	 *      |----|
	 * </pre>
	 */
	@Test
	public void leftOrRightTest3() {
		final MemChunk memChunk = new MemChunk(1, 10);
		final MemChunk other = new MemChunk(5, 7);

		assertFalse(memChunk.isLeftOf(other));
		assertFalse(memChunk.isRightOf(other));
	}

	/**
	 * <pre>
	 * |------------|
	 *                |----|
	 * </pre>
	 */
	@Test
	public void isLeftOf() {
		final MemChunk memChunk = new MemChunk(1, 10);
		final MemChunk other = new MemChunk(11, 15);

		assertTrue(memChunk.isLeftOf(other));
		assertFalse(memChunk.isRightOf(other));
	}

	/**
	 * <pre>
	 *        |------------|
	 * |----|
	 * </pre>
	 */
	@Test
	public void isRightOf() {
		final MemChunk memChunk = new MemChunk(5, 10);
		final MemChunk other = new MemChunk(1, 3);

		assertFalse(memChunk.isLeftOf(other));
		assertTrue(memChunk.isRightOf(other));
	}

	/**
	 * <pre>
	 * |----------|
	 *            |----|
	 * </pre>
	 */
	@Test
	public void isLeftOf1() {
		final MemChunk memChunk = new MemChunk(1, 10);
		final MemChunk other = new MemChunk(11, 15);

		assertTrue(memChunk.isLeftOf(other));
		assertFalse(memChunk.isRightOf(other));
	}

	/**
	 * <pre>
	 *      |----------|
	 * |----|
	 * </pre>
	 */
	@Test
	public void isRightOf1() {
		final MemChunk memChunk = new MemChunk(11, 15);
		final MemChunk other = new MemChunk(1, 10);

		assertFalse(memChunk.isLeftOf(other));
		assertTrue(memChunk.isRightOf(other));
	}

}
