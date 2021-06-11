package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.RetentionRuleDocumentType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by constellios on 2017-04-06.
 */
public class RuleDocumentTypeValidatorAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	RMSchemasRecordsServices rm;

	RecordServices recordServices;

	@Before
	public void setUp() {
		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenRuleEmptyAndCopyEmptyThenOk()
			throws RecordServicesException {

		RetentionRuleDocumentType type = newValidRuleDocumentType()
				.setRule(null).setRuleCopy(null);

		recordServices.add(type.getWrappedRecord());
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void whenRuleEmptyAndCopyNotEmptyThenThrowError()
			throws RecordServicesException {

		RetentionRuleDocumentType type = newValidRuleDocumentType()
				.setRule(null);

		recordServices.add(type.getWrappedRecord());
	}

	@Test
	public void whenCopyRelatedToTheRuleThenOk()
			throws RecordServicesException {

		RetentionRuleDocumentType type = newValidRuleDocumentType();

		recordServices.add(type.getWrappedRecord());
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void whenCopyNotRelatedToTheRuleThenThrowException()
			throws RecordServicesException {

		RetentionRuleDocumentType type = newValidRuleDocumentType()
				.setRuleCopy("fakeCopy");

		recordServices.add(type.getWrappedRecord());
	}

	private RetentionRuleDocumentType newValidRuleDocumentType() {
		RetentionRule rule = rm.getRetentionRule(records.ruleId_1);
		return rm.newRetentionRuleDocumentType()
				.setRule(records.ruleId_1)
				.setRuleCopy(rule.getCopyRetentionRules().get(0).getId());
	}
}
