package de.dirkdittmar.offheapCache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class OffheapByteMapTest {

	private CompactableConcurrentMap<String, byte[]> ccm;
	private OffheapByteMap<String> map;
	
	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		ccm = mock(CompactableConcurrentMap.class);
		map = new OffheapByteMap<>(ccm);
	}
		
	@Test
	public void sizeTest() {
		when(ccm.size()).thenReturn(42);
		assertEquals(42, map.size());
		verify(ccm).size();
	}
	
	@Test
	public void isEmptyTest() {
		when(ccm.isEmpty()).thenReturn(true);
		assertTrue(map.isEmpty());
		verify(ccm).isEmpty();
	}
	
	@Test
	public void containsKeyTest() {
		when(ccm.containsKey("foobar")).thenReturn(true);
		assertTrue(map.containsKey("foobar"));
		verify(ccm).containsKey("foobar");
	}
	
	@Test
	public void containsValueTest() throws UnsupportedEncodingException {
		byte[] bytes = "foobar".getBytes("UTF8");
		when(ccm.containsValue(bytes)).thenReturn(true);
		assertTrue(map.containsValue(bytes));
		verify(ccm).containsValue(bytes);
	}
	
	@Test
	public void getTest() throws UnsupportedEncodingException {
		byte[] bytes = "foobar".getBytes("UTF8");
		when(ccm.get("foobar")).thenReturn(bytes);
		assertTrue(Arrays.equals(bytes, map.get("foobar")));
		verify(ccm).get("foobar");
	}
}
