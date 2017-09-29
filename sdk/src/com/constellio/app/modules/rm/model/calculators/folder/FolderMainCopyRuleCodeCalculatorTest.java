package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilderWithDefinedIds;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FolderMainCopyRuleCodeCalculatorTest extends ConstellioTest {

	@Mock CalculatorParameters parameters;

	CopyRetentionRule mainCopyRule;

	CopyRetentionRuleBuilder copyBuilder = new CopyRetentionRuleBuilderWithDefinedIds();

	@Test
	public void givenMainCopyRuleThenReturnItsCode()
			throws Exception {

		mainCopyRule = copy("2-2-D", "9000");
		assertThat(calculate()).isEqualTo("9000");

		mainCopyRule = copy("2-2-D", null);
		assertThat(calculate()).isEqualTo(null);

		mainCopyRule = null;
		assertThat(calculate()).isEqualTo(null);
	}

	//--------------------------------------------

	private CopyRetentionRule copy(String delays, String code) {
		return copyBuilder.newPrincipal(asList("PA", "MD"), delays).setCode(code);
	}

	private String calculate() {
		FolderMainCopyRuleCodeCalculator calculator = new FolderMainCopyRuleCodeCalculator();

		when(parameters.get(calculator.mainCopyRuleParam)).thenReturn(mainCopyRule);

		calculator.calculate(parameters);
		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}

}
