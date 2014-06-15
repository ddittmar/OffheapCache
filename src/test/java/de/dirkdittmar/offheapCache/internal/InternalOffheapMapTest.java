package de.dirkdittmar.offheapCache.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import de.dirkdittmar.offheapCache.NotEnoughMemException;
import de.dirkdittmar.offheapCache.internal.InternalOffheapMap;

public class InternalOffheapMapTest {

	private final Random random = new Random();

	@Test(expected = IllegalArgumentException.class)
	public void testPutNullValue() {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(100);
		basicCache.put("test", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutNullKey() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(100);
		basicCache.put(null, "foobar".getBytes("UTF8"));
	}

	@Test(expected = NotEnoughMemException.class)
	public void testPutTooMuchStuff() {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(100);

		final byte[] stuff = new byte[1000];
		random.nextBytes(stuff);
		basicCache.put("test", stuff);
	}

	@Test
	public void testPutGetNewValue() {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(100);

		final byte[] stuff = new byte[10];
		random.nextBytes(stuff);
		final byte[] oldValue = basicCache.put("test", stuff);
		assertNull(oldValue);

		final byte[] stuffRead = basicCache.get("test");
		assertTrue(Arrays.equals(stuff, stuffRead));
	}

	@Test
	public void testPutGetReplaceValue() {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);

		final byte[] stuff = new byte[10];
		random.nextBytes(stuff);
		byte[] oldValue = basicCache.put("test", stuff);
		assertNull(oldValue);

		final byte[] stuff2 = new byte[15];
		random.nextBytes(stuff2);
		oldValue = basicCache.put("test", stuff2);
		assertTrue(Arrays.equals(stuff, oldValue));

		final byte[] stuffRead = basicCache.get("test");
		assertTrue(Arrays.equals(stuff2, stuffRead));
	}

	@Test
	public void testOutOfMem() {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);

		final byte[] stuff = new byte[20];
		random.nextBytes(stuff);

		basicCache.put("test1", stuff);
		try {
			basicCache.put("test2", stuff);
			fail("wtf?");
		} catch (final NotEnoughMemException e) {
			System.out.println(String.format("expected: %s", e.getMessage()));
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEntrySet() {
		new InternalOffheapMap<>(20).entrySet();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testValues() {
		new InternalOffheapMap<>(20).values();
	}

	@Test
	public void testContainsValue() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> cache = new InternalOffheapMap<>(20);
		cache.put("foo", "foo".getBytes("UTF8"));
		cache.put("bar", "bar".getBytes("UTF8"));

		assertTrue(cache.containsValue("foo".getBytes("UTF8")));
		assertTrue(cache.containsValue("bar".getBytes("UTF8")));
	}

	@Test
	public void testSize() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> cache = new InternalOffheapMap<>(20);
		assertEquals(0, cache.size());

		cache.put("foo", "foo".getBytes("UTF8"));
		assertEquals(1, cache.size());

		cache.put("bar", "bar".getBytes("UTF8"));
		assertEquals(2, cache.size());
	}

	@Test
	public void testIsEmpty() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> cache = new InternalOffheapMap<>(20);
		assertTrue(cache.isEmpty());

		cache.put("foo", "foo".getBytes("UTF8"));
		assertFalse(cache.isEmpty());
	}

	@Test
	public void testContainsKey() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> cache = new InternalOffheapMap<>(20);

		cache.put("foo", "foo".getBytes("UTF8"));
		assertTrue(cache.containsKey("foo"));

		cache.put("bar", "bar".getBytes("UTF8"));
		assertTrue(cache.containsKey("bar"));
	}

	@Test
	public void testRemove() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> cache = new InternalOffheapMap<>(20);

		final byte[] fooBytes = "foo".getBytes("UTF8");
		cache.put("foo", fooBytes);
		assertTrue(cache.containsKey("foo"));

		final byte[] barBytes = "bar".getBytes("UTF8");
		cache.put("bar", barBytes);
		assertTrue(cache.containsKey("bar"));

		assertTrue(Arrays.equals(fooBytes, cache.remove("foo")));
		assertFalse(cache.containsKey("foo"));
		assertTrue(Arrays.equals(barBytes, cache.remove("bar")));
		assertFalse(cache.containsKey("bar"));

		assertTrue(cache.isEmpty());
	}

	@Test
	public void testAutoCompact() {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);

		// fill the ByteBuffer completely:
		final byte[] stuff1 = new byte[5];
		random.nextBytes(stuff1);
		basicCache.put("test1", stuff1);

		final byte[] stuff2 = new byte[5];
		random.nextBytes(stuff2);
		basicCache.put("test2", stuff2);

		final byte[] stuff3 = new byte[5];
		random.nextBytes(stuff3);
		basicCache.put("test3", stuff3);

		final byte[] stuff4 = new byte[5];
		random.nextBytes(stuff4);
		basicCache.put("test4", stuff4);

		// delete 2 and 4 to get a fragmented ByteBuffer
		assertTrue(Arrays.equals(stuff2, basicCache.remove("test2")));
		assertTrue(Arrays.equals(stuff4, basicCache.remove("test4")));

		final byte[] stuff5 = new byte[10];
		random.nextBytes(stuff5);
		basicCache.put("test5", stuff5);
	}

	@Test
	public void testManuelCompact() {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);

		// fill the ByteBuffer completely:
		final byte[] stuff1 = new byte[5];
		random.nextBytes(stuff1);
		basicCache.put("test1", stuff1);

		final byte[] stuff2 = new byte[5];
		random.nextBytes(stuff2);
		basicCache.put("test2", stuff2);

		final byte[] stuff3 = new byte[5];
		random.nextBytes(stuff3);
		basicCache.put("test3", stuff3);

		final byte[] stuff4 = new byte[5];
		random.nextBytes(stuff4);
		basicCache.put("test4", stuff4);

		// delete 2 and 4 to get a fragmented ByteBuffer
		assertTrue(Arrays.equals(stuff2, basicCache.remove("test2")));
		assertTrue(Arrays.equals(stuff4, basicCache.remove("test4")));

		basicCache.compact();

		final byte[] stuff5 = new byte[10];
		random.nextBytes(stuff5);
		basicCache.put("test5", stuff5);
	}

	@Test
	public void testClear() {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);
		assertTrue(basicCache.isEmpty());

		// fill the ByteBuffer completely:
		final byte[] stuff1 = new byte[5];
		random.nextBytes(stuff1);
		basicCache.put("test1", stuff1);

		final byte[] stuff2 = new byte[5];
		random.nextBytes(stuff2);
		basicCache.put("test2", stuff2);

		final byte[] stuff3 = new byte[5];
		random.nextBytes(stuff3);
		basicCache.put("test3", stuff3);

		final byte[] stuff4 = new byte[5];
		random.nextBytes(stuff4);
		basicCache.put("test4", stuff4);

		assertFalse(basicCache.isEmpty());

		// delete 2 and 4 to get a fragmented ByteBuffer
		assertTrue(Arrays.equals(stuff2, basicCache.remove("test2")));
		assertTrue(Arrays.equals(stuff4, basicCache.remove("test4")));

		assertFalse(basicCache.isEmpty());

		basicCache.clear();
		assertTrue(basicCache.isEmpty());
	}

	@Test
	public void testPutAll() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);

		final Map<String, byte[]> testMap = new HashMap<>();
		testMap.put("foo", "foo".getBytes("UTF8"));
		testMap.put("bar", "bar".getBytes("UTF8"));

		basicCache.putAll(testMap);

		assertTrue(basicCache.containsKey("foo"));
		assertTrue(Arrays.equals(basicCache.get("foo"), "foo".getBytes("UTF8")));
		assertTrue(basicCache.containsKey("bar"));
		assertTrue(Arrays.equals(basicCache.get("bar"), "bar".getBytes("UTF8")));
	}

	@Test
	public void testKeySet() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);

		basicCache.put("foo", "foo".getBytes("UTF8"));
		basicCache.put("bar", "bar".getBytes("UTF8"));

		assertTrue(basicCache.keySet().contains("foo"));
		assertTrue(basicCache.keySet().contains("bar"));
	}

	@Test
	public void testPutIfAbsent() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);
		assertNull(basicCache.putIfAbsent("foo", "foo".getBytes("UTF8")));
		assertTrue(Arrays.equals("foo".getBytes("UTF8"),
				basicCache.putIfAbsent("foo", "bar".getBytes("UTF8"))));
	}

	@Test
	public void testRemove2() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);
		basicCache.put("foo", "foo".getBytes("UTF8"));
		basicCache.put("bar", "bar".getBytes("UTF8"));

		assertTrue(basicCache.remove("foo", "foo".getBytes("UTF8")));
		assertFalse(basicCache.remove("bar", "foo".getBytes("UTF8")));
		assertFalse(basicCache.remove("foobar", "foobar".getBytes("UTF8")));
	}

	@Test
	public void testReplace2() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);
		basicCache.put("foo", "foo".getBytes("UTF8"));
		basicCache.put("bar", "bar".getBytes("UTF8"));

		assertTrue(basicCache.replace("foo", "foo".getBytes("UTF8"),
				"bar".getBytes("UTF8")));
		assertTrue(Arrays.equals("bar".getBytes("UTF8"), basicCache.get("foo")));

		assertFalse(basicCache.replace("bar", "foo".getBytes("UTF8"),
				"foobar".getBytes("UTF8")));
		assertTrue(Arrays.equals("bar".getBytes("UTF8"), basicCache.get("bar")));

		assertFalse(basicCache.replace("foobar", "foobar".getBytes("UTF8"),
				"foobar".getBytes("UTF8")));
	}

	@Test
	public void testReplace() throws UnsupportedEncodingException {
		final InternalOffheapMap<String> basicCache = new InternalOffheapMap<>(20);
		basicCache.put("foo", "foo".getBytes("UTF8"));
		basicCache.put("bar", "bar".getBytes("UTF8"));

		assertNull(basicCache.replace("foobar", "foobar".getBytes("UTF8")));
		assertTrue(Arrays.equals("foo".getBytes("UTF8"),
				basicCache.replace("foo", "bar".getBytes("UTF8"))));
	}
}
