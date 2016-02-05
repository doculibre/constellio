package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListUniformCopyTypeCalculatorTest extends ConstellioTest {

	CopyType principalCopyType, secndaryCopyType, copyType;
	List<CopyType> equalsValues;
	List<CopyType> differentsValues;
	List<CopyType> emptyList;
	List<CopyType> copyTypes;
	DecomListUniformCopyTypeCalculator calculator;
	@Mock CalculatorParameters parameters;

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new DecomListUniformCopyTypeCalculator());

		principalCopyType = CopyType.PRINCIPAL;
		secndaryCopyType = CopyType.SECONDARY;

		equalsValues = Arrays.asList(principalCopyType, principalCopyType, principalCopyType);
		differentsValues = Arrays.asList(principalCopyType, principalCopyType, secndaryCopyType);
		emptyList = new ArrayList<>();
	}

	@Test
	public void givenDifferentsValuesInListParamsWhenCalculateThenReturnNull()
			throws Exception {

		copyTypes = differentsValues;

		copyType = calculatedValue();

		assertThat(copyType).isNull();
	}

	@Test
	public void givenEqualsValuesInListParamsWhenCalculateThenReturnNull()
			throws Exception {

		copyTypes = equalsValues;

		copyType = calculatedValue();

		assertThat(copyType).isEqualTo(principalCopyType);
	}

	@Test
	public void givenEmptyListParamsWhenCalculateThenReturnNull()
			throws Exception {

		copyTypes = emptyList;

		copyType = calculatedValue();

		assertThat(copyType).isNull();
	}

	// --------------------

	private CopyType calculatedValue() {

		when(parameters.get(calculator.foldersCopyTypesParam)).thenReturn(copyTypes);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
