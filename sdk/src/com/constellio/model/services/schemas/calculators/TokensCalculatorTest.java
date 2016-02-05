package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class TokensCalculatorTest extends ConstellioTest {

	String auth1 = "r_zeRole_auth1";
	String auth2 = "rw__auth2";
	String auth3 = "rwd_role1,role2_auth3";
	String auth4 = "_role1,role2_auth4";
	@Mock CalculatorParameters parameters;

	List<String> auths;

	TokensCalculator2 calculator;

	LocalDependency<List<String>> allAuthorizationsParam = LocalDependency
			.toAStringList(CommonMetadataBuilder.ALL_AUTHORIZATIONS);
	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(CommonMetadataBuilder.MANUAL_TOKENS);

	@Before
	public void setUp()
			throws Exception {
		calculator = new TokensCalculator2();

		auths = new ArrayList<>();

		auths.add(auth1);
		auths.add(auth2);
		auths.add(auth3);
		auths.add(auth4);

		when(parameters.get(allAuthorizationsParam)).thenReturn(auths);
		when(parameters.get(manualTokensParam)).thenReturn(Arrays.asList(Record.PUBLIC_TOKEN));
	}

	@Test
	public void whenCalculatingThenAllTokensOk()
			throws Exception {
		List<String> calculatedAuths = calculator.calculate(parameters);

		assertThat(calculatedAuths)
				.containsOnly("r_zeRole_auth1", "r__auth2", "w__auth2", "r_role1,role2_auth3", "w_role1,role2_auth3",
						"d_role1,role2_auth3", "_role1,role2_auth4", Record.PUBLIC_TOKEN);
	}

	@Test
	public void whenGettingReturnTypeThenText()
			throws Exception {
		assertThat(calculator.getReturnType()).isEqualTo(STRING);
	}

	@Test
	public void whenGettingDependenciesThenRightValueReturned()
			throws Exception {
		assertThat((List) calculator.getDependencies())
				.containsOnly(allAuthorizationsParam, manualTokensParam);
	}

	@Test
	public void whenCheckingIfMultivalueThenTrue()
			throws Exception {
		assertThat(calculator.isMultiValue()).isTrue();
	}

}
