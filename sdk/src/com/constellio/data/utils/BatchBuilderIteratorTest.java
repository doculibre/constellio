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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class BatchBuilderIteratorTest extends ConstellioTest {

	Iterator<String> nestedIterator;
	BatchBuilderIterator<String> iterator;

	@Before
	public void setUp()
			throws Exception {

	}

	@Test
	public void whenIteratingOnEmptyNestedIteratorThenHasNextIsFalse()
			throws Exception {

		nestedIterator = Collections.emptyIterator();
		iterator = new BatchBuilderIterator<>(nestedIterator, 3);

		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void whenIteratingOn3ItemsNestedIteratorThenReturnOneBatch()
			throws Exception {

		nestedIterator = Arrays.asList("a", "b", "c").iterator();
		iterator = new BatchBuilderIterator<>(nestedIterator, 3);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(Arrays.asList("a", "b", "c"));
		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void whenIteratingOn4ItemsNestedIteratorThenReturnTwoBatch()
			throws Exception {

		nestedIterator = Arrays.asList("a", "b", "c", "d").iterator();
		iterator = new BatchBuilderIterator<>(nestedIterator, 3);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(Arrays.asList("a", "b", "c"));
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(Arrays.asList("d"));
		assertThat(iterator.hasNext()).isFalse();

	}
}
