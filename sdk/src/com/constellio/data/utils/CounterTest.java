package com.constellio.data.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

public class CounterTest {

	private Counter counter;

	@Before
	public void setUp() {
		counter = new Counter();
	}

	@Test
	public void whenCounterCreatedThenSetToZero() {
		assertEquals(0, counter.current());
	}

	@Test
	public void whenIncrementThenIncrement() {
		Counter counter = new Counter();
		assertEquals(1, counter.increment());
		assertEquals(1, counter.current());
		assertEquals(2, counter.increment());
		assertEquals(2, counter.current());
	}

	@Test
	public void whenResetThenSetToZero() {
		Counter counter = new Counter();
		counter.increment();
		counter.increment();
		counter.reset();

		assertEquals(0, counter.current());
	}

	@Test
	public void whenUseGlobalCounterThenCreateAndReuseSameInstance() {
		Counter counter1 = Counter.global();
		Counter counter2 = Counter.global();
		assertSame(counter1, counter2);
	}
}
