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
