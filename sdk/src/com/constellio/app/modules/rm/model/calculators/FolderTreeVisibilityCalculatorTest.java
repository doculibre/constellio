package com.constellio.app.modules.rm.model.calculators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderTreeVisibilityCalculatorTest extends ConstellioTest {
	@Mock CalculatorParameters parameters;

	FolderTreeVisibilityCalculator calculator;

	@Before
	public void setUp() {
		calculator = new FolderTreeVisibilityCalculator();
	}

	@Test
	public void givenTheStatusIsActiveThenReturnsTrue() {
		when(parameters.get(calculator.folderStatus)).thenReturn(FolderStatus.ACTIVE);

		assertThat(calculate()).isTrue();
	}

	@Test
	public void givenTheStatusIsSemiActiveThenReturnsSemiActiveConfig() {
		when(parameters.get(calculator.folderStatus)).thenReturn(FolderStatus.SEMI_ACTIVE);

		when(parameters.get(calculator.displaySemiActive)).thenReturn(true);
		assertThat(calculate()).isTrue();

		when(parameters.get(calculator.displaySemiActive)).thenReturn(false);
		assertThat(calculate()).isFalse();
	}

	@Test
	public void givenTheStatusIsDepositedThenReturnsDepositedConfig() {
		when(parameters.get(calculator.folderStatus)).thenReturn(FolderStatus.INACTIVE_DEPOSITED);

		when(parameters.get(calculator.displayDeposited)).thenReturn(true);
		assertThat(calculate()).isTrue();

		when(parameters.get(calculator.displayDeposited)).thenReturn(false);
		assertThat(calculate()).isFalse();
	}

	@Test
	public void givenTheStatusIsDestroyedThenReturnsDestroyedConfig() {
		when(parameters.get(calculator.folderStatus)).thenReturn(FolderStatus.INACTIVE_DESTROYED);

		when(parameters.get(calculator.displayDestroyed)).thenReturn(true);
		assertThat(calculate()).isTrue();

		when(parameters.get(calculator.displayDestroyed)).thenReturn(false);
		assertThat(calculate()).isFalse();
	}

	private Boolean calculate() {
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
