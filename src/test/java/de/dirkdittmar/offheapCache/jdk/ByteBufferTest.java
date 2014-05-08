package de.dirkdittmar.offheapCache.jdk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;
import static org.junit.Assert.*;

public class ByteBufferTest {
	
	@Test
	public void defaultByteOrderTest() {
		ByteBuffer buf1 = ByteBuffer.allocateDirect(100);
		assertTrue(ByteOrder.BIG_ENDIAN == buf1.order());
	}

	@Test
	public void duplicateBugTest() {
		ByteBuffer buf1 = ByteBuffer.allocateDirect(100);
		buf1.order(ByteOrder.BIG_ENDIAN);
		assertTrue(ByteOrder.BIG_ENDIAN == buf1.order());
		
		ByteBuffer buf2 = buf1.duplicate();
		assertTrue(ByteOrder.BIG_ENDIAN == buf2.order());
		
		buf1.order(ByteOrder.LITTLE_ENDIAN);
		assertTrue(ByteOrder.LITTLE_ENDIAN == buf1.order());
		buf2 = buf1.duplicate();
		// Hier ist der BUG!
		// Man sollte ja annehmen das man beim duplizieren eines Little-Endian-Buffers
		// auch wieder einen Little-Endian Buffer erhält.
		assertFalse(ByteOrder.LITTLE_ENDIAN == buf2.order());
	}
	
	@Test
	public void readOnlyBugTest() {
		ByteBuffer buf1 = ByteBuffer.allocateDirect(100);
		buf1.order(ByteOrder.BIG_ENDIAN);
		assertTrue(ByteOrder.BIG_ENDIAN == buf1.order());
		
		ByteBuffer buf2 = buf1.asReadOnlyBuffer();
		assertTrue(ByteOrder.BIG_ENDIAN == buf2.order());
		
		buf1.order(ByteOrder.LITTLE_ENDIAN);
		assertTrue(ByteOrder.LITTLE_ENDIAN == buf1.order());
		buf2 = buf1.asReadOnlyBuffer();
		// Hier ist der BUG!
		// Man sollte ja annehmen das man beim duplizieren eines Little-Endian-Buffers
		// auch wieder einen Little-Endian Buffer erhält.
		assertFalse(ByteOrder.LITTLE_ENDIAN == buf2.order());
	}
	
}
