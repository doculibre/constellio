package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListStatusCalculatorTest extends ConstellioTest {
	DecomListStatusCalculator2 calculator;

	@Mock CalculatorParameters parameters;
	LocalDate processingDate;
	LocalDate approvalDate;
	LocalDate approvalRequestDate;
	List<DecomListValidation> validations;

	@Before
	public void setUp() {
		processingDate = null;
		approvalDate = null;
		approvalRequestDate = null;
		validations = new ArrayList<>();
		calculator = new DecomListStatusCalculator2();
	}

	@Test
	public void givenProcessingDateIsSetThenStatusIsProcessed() {
		processingDate = TimeProvider.getLocalDate();
		assertThat(calculatedValue()).isEqualTo(DecomListStatus.PROCESSED);
	}

	@Test
	public void givenApprovalDateIsSetAndProcessingDateIsNullThenStatusIsApproved() {
		approvalDate = TimeProvider.getLocalDate();
		assertThat(calculatedValue()).isEqualTo(DecomListStatus.APPROVED);
	}

	@Test
	public void givenPendingValidationRequestsAndApprovalDateIsNullThenStatusIsInValidation() {
		validations.add(new DecomListValidation("SOME_ID", TimeProvider.getLocalDate()));
		assertThat(calculatedValue()).isEqualTo(DecomListStatus.IN_VALIDATION);

		approvalRequestDate = TimeProvider.getLocalDate().minusDays(1);
		assertThat(calculatedValue()).isEqualTo(DecomListStatus.IN_VALIDATION);
	}

	@Test
	public void givenApprovalRequestDateIsSetAndValidationsEmptyThenStatusIsInApproval() {
		approvalRequestDate = TimeProvider.getLocalDate().minusDays(1);
		assertThat(calculatedValue()).isEqualTo(DecomListStatus.IN_APPROVAL);
	}

	@Test
	public void givenApprovalRequestDateIsSetAndValidationsDoneThenStatusIsInApproval() {
		validations.add(new DecomListValidation(
				"SOME_ID", TimeProvider.getLocalDate().minusDays(1)).setValidationDate(TimeProvider.getLocalDate()));
		approvalRequestDate = TimeProvider.getLocalDate();
		assertThat(calculatedValue()).isEqualTo(DecomListStatus.IN_APPROVAL);
	}

	@Test
	public void givenValidationsAreDoneAndAllDatesAreNullThenStatusIsValidated() {
		validations.add(new DecomListValidation(
				"SOME_ID", TimeProvider.getLocalDate().minusDays(1)).setValidationDate(TimeProvider.getLocalDate()));
		assertThat(calculatedValue()).isEqualTo(DecomListStatus.VALIDATED);
	}

	@Test
	public void givenAllDatesAreNullAndValidationsEmptyThenStatusIsGenerated() {
		assertThat(calculatedValue()).isEqualTo(DecomListStatus.GENERATED);
	}

	private DecomListStatus calculatedValue() {
		when(parameters.get(calculator.processingDate)).thenReturn(processingDate);
		when(parameters.get(calculator.approvalDate)).thenReturn(approvalDate);
		when(parameters.get(calculator.approvalRequestDate)).thenReturn(approvalRequestDate);
		when(parameters.get(calculator.validations)).thenReturn(validations);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
