package com.constellio.model.entities.schemas.sort;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class DefaultStringSortFieldNormalizerTest extends ConstellioTest {

	DefaultStringSortFieldNormalizer normalizer = new DefaultStringSortFieldNormalizer();

	@Test
	public void whenNormalizeNullThenReturnNullValue()
			throws Exception {

		assertThat(normalizer.normalizeNull()).isNull();

		assertThatElementsAreSortedInOrder("A1", "A10", "A100", "A100 0", "A100 1", "A100 10",
				"A100 100", "A100 100 0", "A100 100 10", "A100 100 100");

		assertThatElementsAreSortedInOrder("É1", "E10", "è10 100", "e10-100", "è10-101", "É100", "È1000", "e10000");
	}

	private void assertThatElementsAreSortedInOrder(String... strings) {

		List<String> normalizedStrings = new ArrayList<>();
		for (String string : strings) {
			normalizedStrings.add(normalizer.normalize(string));
		}

		for (int i = 0; i < normalizedStrings.size() - 1; i++) {
			String normalizedStringBefore = normalizedStrings.get(i);
			String normalizedStringAfter = normalizedStrings.get(i + 1);
			assertThat(normalizedStringBefore.compareTo(normalizedStringAfter)).describedAs(
					"Element at position " + i + " with normalized text '" + normalizedStringBefore + "' is expected to be " +
							"before element at position " + (i + 1) + " with normalized text '" + normalizedStringAfter + "'")
					.isLessThan(0);
		}

	}
}
