package com.constellio.data.utils;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyMergingIteratorTest extends ConstellioTest {

	@Test
	public void givenMultipleIteratorsReturningResultsWithoutUniqueKeysConversionThenAllReturned() {

		List<Iterator<String>> iterators = new ArrayList<>();
		iterators.add(Arrays.asList("1", "3", "42", "34").iterator());
		iterators.add(new ArrayList<String>().iterator());
		iterators.add(new ArrayList<String>().iterator());
		iterators.add(Arrays.asList("12", "3", "42", "32", "3").iterator());

		assertThat(new LazyMergingIterator<>(iterators)).containsExactly("1", "3", "42", "34", "12", "3", "42", "32", "3");

	}


	@Test
	public void givenMultipleIteratorsReturningResultsWithUniqueKeysConversionThenDoNotReturnDuplicateValues() {

		List<Iterator<String>> iterators = new ArrayList<>();
		iterators.add(Arrays.asList("1", "3", "42", "34").iterator());
		iterators.add(new ArrayList<String>().iterator());
		iterators.add(new ArrayList<String>().iterator());
		iterators.add(Arrays.asList("12", "3", "42", "32", "3").iterator());

		assertThat(new LazyMergingIterator<String>(iterators) {

			@Override
			protected String toUniqueKey(String element) {
				return element;
			}
		}).containsExactly("1", "3", "42", "34", "12", "32");
	}
}
