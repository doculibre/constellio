package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SavedSearchRestrictedCalculatorAcceptanceTest extends ConstellioTest {

	@Mock private CalculatorParameters parameters;

	private SavedSearchRestrictedCalculator calculator;
	private LocalDependency<List<String>> shareGroupsParams = LocalDependency.toAStringList(SavedSearch.SHARED_GROUPS);
	private LocalDependency<List<String>> shareUsersParams = LocalDependency.toAStringList(SavedSearch.SHARED_USERS);

	@Before
	public void setUp()
			throws Exception {
		calculator = new SavedSearchRestrictedCalculator();

		when(parameters.get(shareGroupsParams)).thenReturn(null);
		when(parameters.get(shareUsersParams)).thenReturn(null);
	}

	@Test
	public void givenSharedGroupsIsNotEmptyThenTrue() {
		when(parameters.get(shareGroupsParams)).thenReturn(Collections.singletonList("12345"));

		assertThat(calculator.calculate(parameters)).isTrue();
	}

	@Test
	public void givenSharedUsersIsNotEmptyThenTrue() {
		when(parameters.get(shareUsersParams)).thenReturn(Collections.singletonList("12345"));

		assertThat(calculator.calculate(parameters)).isTrue();
	}

	@Test
	public void givenSharedGroupsIsEmptyAndSharedUsersIsEmptyThenFalse() {
		when(parameters.get(shareUsersParams)).thenReturn(Collections.<String>emptyList());
		when(parameters.get(shareUsersParams)).thenReturn(Collections.<String>emptyList());

		assertThat(calculator.calculate(parameters)).isFalse();
	}

	@Test
	public void givenSharedGroupsIsNullAndSharedUsersIsNullThenFalse() {
		assertThat(calculator.calculate(parameters)).isFalse();
	}
}
