package de.dirkdittmar.offheapCache.jdk;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Test;

public class ReadWriteLockTest {

	private ReadWriteLock rwLock = new ReentrantReadWriteLock(true);

	@Test
	public void writeThreadLockedUntilNoReaders() {
		try {
			ReadThread rt1 = new ReadThread();
			rt1.start();
			ReadThread rt2 = new ReadThread();
			rt2.start();
			
			WriteThread wt1 = new WriteThread();
			wt1.start();
			
			ReadThread rt3 = new ReadThread();
			rt3.start();
			
			wt1.join();
			rt1.join();
			rt2.join();
			rt3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class ReadThread extends Thread {

		public ReadThread() {
			this.setName("ReadThread" + this.getId());
		}
		
		public void run() {
			Lock readLock = rwLock.readLock();
			readLock.lock();
			try {
				System.out.println(String.format("[%s] %s", this.getName(), "read..."));
				sleep(1000);
				System.out.println(String.format("[%s] %s", this.getName(), "done..."));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				readLock.unlock();
			}
		}

	}

	private class WriteThread extends Thread {

		public WriteThread() {
			this.setName("WriteThread" + this.getId());
		}
		
		public void run() {
			Lock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				System.out.println(String.format("[%s] %s", this.getName(), "write..."));
				sleep(1000);
				System.out.println(String.format("[%s] %s", this.getName(), "done..."));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			writeLock.unlock();
		}

	}

}
