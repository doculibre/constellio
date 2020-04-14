package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RuleMediumTypeReferencesCalculatorAcceptanceTest extends ConstellioTest {

	private RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);

	private RetentionRule retentionRule;
	private Users users = new Users();

	@Before
	public void setUp() {
		prepareSystem(withZeCollection()
				.withConstellioRMModule()
				.withAllTest(users)
				.withRMTest(records));

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	private RetentionRule newRetentionRule() {
		return rm.newRetentionRuleWithId("133").setCode("SnD").setTitle("1m");
	}

	private List<String> pushAndFetchRefenrecesInRule() throws RecordServicesException {
		rm.executeTransaction(new Transaction(retentionRule));
		return rm.get(retentionRule.getId()).get(Schemas.ALL_REFERENCES);
	}

	@Test
	public void whenReferenceToMediumTypeOnly() throws RecordServicesException {
		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();
		CopyRetentionRule principal = copyBuilder.newPrincipal(asList(records.PA), "2-888-D");
		CopyRetentionRule secondary = copyBuilder.newSecondary(asList(records.MD), "3-999-T");

		retentionRule = newRetentionRule()
				.setCopyRetentionRules(principal, secondary)
				.setResponsibleAdministrativeUnits(true);
		assertThat(pushAndFetchRefenrecesInRule()).containsOnly(rm.PA(), rm.DM());
	}

	@Test
	public void whenReferencesInAndOutOfStructure() throws RecordServicesException {
		List<String> adminUnits = asList(records.unitId_10a);

		retentionRule = newRetentionRule().setAdministrativeUnits(adminUnits);

		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();
		CopyRetentionRule principal = copyBuilder.newPrincipal(asList(records.PA), "2-888-D");
		CopyRetentionRule secondary = copyBuilder.newSecondary(asList(records.MD), "3-999-T");

		retentionRule.setCopyRetentionRules(principal, secondary);

		assertThat(pushAndFetchRefenrecesInRule()).containsOnly(records.unitId_10a, rm.PA(), rm.DM());
	}
}
