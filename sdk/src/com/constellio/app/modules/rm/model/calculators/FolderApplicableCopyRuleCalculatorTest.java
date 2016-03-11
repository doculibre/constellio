package com.constellio.app.modules.rm.model.calculators;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderApplicableCopyRuleCalculatorTest extends ConstellioTest {

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	CopyRetentionRule principal, secondPrincipal, thirdPrincipal, secondary;
	List<String> mediumTypes;
	CopyType folderCopyType;

	List<String> PA_DM = asList("PA", "DM");

	FolderApplicableCopyRuleCalculator calculator;

	@Mock CalculatorParameters parameters;

	CopyRetentionRuleBuilder builder;

	@Before
	public void setUp()
			throws Exception {
		calculator = spy(new FolderApplicableCopyRuleCalculator());
		builder = new CopyRetentionRuleBuilder(new UUIDV1Generator());
	}

	@Test
	public void givenFolderHasSecondaryCopyTypeThenReturnTheSecondary() {
		principal = copyBuilder.newPrincipal(PA_DM, "888-0-D");
		secondary = copyBuilder.newSecondary(PA_DM, "888-0-C");
		mediumTypes = PA_DM;
		folderCopyType = CopyType.SECONDARY;

		assertThat(calculatedValue()).containsOnlyOnce(secondary);

	}

	@Test
	public void givenFolderHasPrincipalCopyTypeAndRuleWithSinglePrincipalCopyThenReturThePrincipal() {
		principal = copyBuilder.newPrincipal(PA_DM, "888-0-D");
		secondary = copyBuilder.newSecondary(PA_DM, "888-0-C");
		mediumTypes = PA_DM;
		folderCopyType = CopyType.PRINCIPAL;

		assertThat(calculatedValue()).containsOnlyOnce(principal);

	}

	@Test
	public void givenFolderHasASingleMediumTypeMatchWithARuleCopyThenMatchReturned() {
		secondary = copyBuilder.newSecondary(asList("PA", "FI"), "888-0-C");
		principal = copyBuilder.newPrincipal(asList("PA"), "888-0-D");
		secondPrincipal = copyBuilder.newPrincipal(asList("FI"), "888-0-D");
		folderCopyType = CopyType.PRINCIPAL;

		mediumTypes = Arrays.asList("PA");
		assertThat(calculatedValue()).containsOnlyOnce(principal);

		mediumTypes = Arrays.asList("FI");
		assertThat(calculatedValue()).containsOnlyOnce(secondPrincipal);

		mediumTypes = Arrays.asList("DM", "PA");
		assertThat(calculatedValue()).containsOnlyOnce(principal);

		mediumTypes = Arrays.asList("DM", "FI");
		assertThat(calculatedValue()).containsOnlyOnce(secondPrincipal);

		mediumTypes = Arrays.asList("PA", "DM");
		assertThat(calculatedValue()).containsOnlyOnce(principal);

		mediumTypes = Arrays.asList("FI", "DM");
		assertThat(calculatedValue()).containsOnlyOnce(secondPrincipal);

		mediumTypes = Arrays.asList("Z6", "PA", "Z2");
		principal = copyBuilder.newPrincipal(asList("Z1", "PA", "Z3"), "888-0-D");
		secondPrincipal = copyBuilder.newPrincipal(asList("Z4", "FI", "Z5"), "888-0-D");
		assertThat(calculatedValue()).containsOnlyOnce(principal);

		mediumTypes = Arrays.asList("Z6", "FI", "Z2");
		principal = copyBuilder.newPrincipal(asList("Z1", "PA", "Z3"), "888-0-D");
		secondPrincipal = copyBuilder.newPrincipal(asList("Z4", "FI", "Z5"), "888-0-D");
		assertThat(calculatedValue()).containsOnlyOnce(secondPrincipal);

		mediumTypes = Arrays.asList("Z6", "FI", "Z5");
		principal = copyBuilder.newPrincipal(asList("Z1", "PA", "Z3"), "888-0-D");
		secondPrincipal = copyBuilder.newPrincipal(asList("Z4", "FI", "Z5"), "888-0-D");
		assertThat(calculatedValue()).containsOnlyOnce(secondPrincipal);

	}

	@Test
	public void givenFolderHasNoMatchWithARuleCopyThenPrincipalCopyChoosedAmongAllCopyRulesBasedOnTheDecommissioningDate() {
		secondary = copyBuilder.newSecondary(asList("PA", "FI"), "888-0-C");
		principal = copyBuilder.newPrincipal(asList("PA"), "888-0-D");
		secondPrincipal = copyBuilder.newPrincipal(asList("FI"), "888-0-D");
		thirdPrincipal = copyBuilder.newPrincipal(asList("PA", "FI"), "888-0-D");
		folderCopyType = CopyType.PRINCIPAL;
		mediumTypes = Arrays.asList("DM");

		assertThat(calculatedValue()).containsExactly(principal, secondPrincipal, thirdPrincipal);

	}

	@Test
	public void givenFolderHasTwoMatchWithARuleCopyThenPrincipalCopyChoosedAmongMatchedRulesBasedOnTheDecommissioningDate() {
		secondary = copyBuilder.newSecondary(asList("PA", "FI"), "888-0-C");
		principal = copyBuilder.newPrincipal(asList("PA"), "888-0-D");
		secondPrincipal = copyBuilder.newPrincipal(asList("FI"), "888-0-D");
		thirdPrincipal = copyBuilder.newPrincipal(asList("PA", "FI"), "888-0-D");
		folderCopyType = CopyType.PRINCIPAL;
		mediumTypes = Arrays.asList("PA");

		assertThat(calculatedValue()).containsExactly(principal, thirdPrincipal);

	}

	// --------------------

	private List<CopyRetentionRule> calculatedValue() {
		when(parameters.get(calculator.folderCopyTypeParam)).thenReturn(folderCopyType);
		when(parameters.get(calculator.folderMediumTypesParam)).thenReturn(mediumTypes);

		List<CopyRetentionRule> copyRetentionRules = new ArrayList<>();
		if (principal != null) {
			copyRetentionRules.add(principal);
		}
		if (secondPrincipal != null) {
			copyRetentionRules.add(secondPrincipal);
		}

		if (thirdPrincipal != null) {
			copyRetentionRules.add(thirdPrincipal);
		}

		if (secondary != null) {
			copyRetentionRules.add(secondary);
		}

		when(parameters.get(calculator.ruleCopyRulesParam)).thenReturn(copyRetentionRules);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
