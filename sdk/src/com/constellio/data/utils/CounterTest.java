/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
