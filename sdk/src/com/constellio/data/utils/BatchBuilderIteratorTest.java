package com.constellio.data.utils;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

		nestedIterator = asList("a", "b", "c").iterator();
		iterator = new BatchBuilderIterator<>(nestedIterator, 3);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(asList("a", "b", "c"));
		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void whenIteratingOn4ItemsNestedIteratorThenReturnTwoBatch()
			throws Exception {

		nestedIterator = asList("a", "b", "c", "d").iterator();
		iterator = new BatchBuilderIterator<>(nestedIterator, 3);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(asList("a", "b", "c"));
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(asList("d"));
		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void whenIteratingOnListIteratorThenOk()
			throws Exception {

		List<List<String>> lists = new ArrayList<>();
		lists.add(asList("a", "b", "c", "d"));
		lists.add(null);
		lists.add(new ArrayList<String>());
		lists.add(asList("e", "f", "g"));

		iterator = BatchBuilderIterator.forListIterator(lists.iterator(), 3);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(asList("a", "b", "c"));
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(asList("d", "e", "f"));
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(asList("g"));
		assertThat(iterator.hasNext()).isFalse();

	}
}
