package com.constellio.app.modules.rm.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class RMSchemasDeleteAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Test
	public void whenCallLogicallyThenPhysicallyDeletableCheckOnCategoriesThenGoodBehavior() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		User admin = records.getAdmin();

		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X100(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X110(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X120(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X13(), admin).isTrue();

		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z100(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z110(), admin).isTrue();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z111(), admin).isTrue();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z112(), admin).isTrue();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z120(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z200(), admin).isTrue();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z999(), admin).isTrue();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_ZE42(), admin).isTrue();

		recordServices.logicallyDelete(records.getCategory_Z110().getWrappedRecord(), admin);
		recordServices.physicallyDelete(records.getCategory_Z110().getWrappedRecord(), admin);

		recordServices.logicallyDelete(records.getCategory_X13().getWrappedRecord(), admin);
		recordServices.physicallyDelete(records.getCategory_X13().getWrappedRecord(), admin);
	}

	@Test
	public void givenSystemWithFoldersAndDocumentsThenRetentionRulesNotDeletable() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		User admin = records.getAdmin();

		assertThatLogicallyDeletable(records.getRule1(), admin).isFalse();
		assertThatLogicallyDeletable(records.getRule2(), admin).isFalse();
		assertThatLogicallyDeletable(records.getRule3(), admin).isFalse();
		assertThatLogicallyDeletable(records.getRule4(), admin).isFalse();
		assertThatLogicallyDeletable(records.getRule5(), admin).isTrue();

	}

	@Test
	public void givenSystemWithoutFoldersAndDocumentsThenRetentionRulesDeletable() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		User admin = records.getAdmin();

		assertThat(records.getCategory_ZE42().getRententionRules())
				.contains(records.ruleId_1, records.ruleId_2, records.ruleId_3, records.ruleId_4);

		assertThatLogicallyDeletable(records.getRule1(), admin).isTrue();
		assertThatLogicallyDeletable(records.getRule2(), admin).isTrue();
		assertThatLogicallyDeletable(records.getRule3(), admin).isTrue();
		assertThatLogicallyDeletable(records.getRule4(), admin).isTrue();
		assertThatLogicallyDeletable(records.getRule5(), admin).isTrue();

		recordServices.logicallyDelete(records.getRule1().getWrappedRecord(), admin);
		recordServices.physicallyDelete(records.getRule1().getWrappedRecord(), admin);
		recordServices.logicallyDelete(records.getRule2().getWrappedRecord(), admin);
		recordServices.physicallyDelete(records.getRule2().getWrappedRecord(), admin);
		recordServices.logicallyDelete(records.getRule3().getWrappedRecord(), admin);
		recordServices.physicallyDelete(records.getRule3().getWrappedRecord(), admin);
		recordServices.logicallyDelete(records.getRule4().getWrappedRecord(), admin);
		recordServices.physicallyDelete(records.getRule4().getWrappedRecord(), admin);
		recordServices.logicallyDelete(records.getRule5().getWrappedRecord(), admin);
		recordServices.physicallyDelete(records.getRule5().getWrappedRecord(), admin);

		assertThat(records.getCategory_ZE42().getRententionRules())
				.isEmpty();
	}

	@Test
	public void whenCallLogicallyDeletableCheckOnCategoriesThenGoodBehavior() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		User admin = records.getAdmin();

		assertThatLogicallyDeletable(records.getCategory_X(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_X100(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_X110(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_X120(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_X13(), admin).isTrue();

		assertThatLogicallyDeletable(records.getCategory_Z(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z100(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z110(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z110(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z111(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z112(), admin).isTrue();

		//Logically deletable, but not physically
		assertThatLogicallyDeletable(records.getCategory_Z120(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z200(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z999(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_ZE42(), admin).isTrue();
	}

	private org.assertj.core.api.BooleanAssert assertThatLogicallyDeletable(RecordWrapper recordWrapper,
			User user) {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		return assertThat(recordServices.isLogicallyDeletable(recordWrapper.getWrappedRecord(), user));
	}

	private org.assertj.core.api.BooleanAssert assertThatLogicallyThenPhysicallyDeletable(RecordWrapper recordWrapper,
			User user) {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		return assertThat(recordServices.isLogicallyThenPhysicallyDeletable(recordWrapper.getWrappedRecord(), user));
	}

	@Test
	public void whenLogicallyDeletingVariableRetentionPeriodThenOnlyPossibleIfNotUsedAndNot888And999()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withFoldersAndContainersOfEveryStatus()
		);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(rm.newVariableRetentionPeriod().setCode("42").setTitle("42"));
		transaction.add(rm.newVariableRetentionPeriod().setCode("666").setTitle("666"));
		recordServices.execute(transaction);

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		VariableRetentionPeriod period888 = rm.PERIOD_888();
		VariableRetentionPeriod period999 = rm.PERIOD_999();
		VariableRetentionPeriod period42 = rm.getVariableRetentionPeriodWithCode("42");
		VariableRetentionPeriod period666 = rm.getVariableRetentionPeriodWithCode("666");

		assertThat(recordServices.isLogicallyDeletable(period888.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period999.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period42.getWrappedRecord(), admin)).isTrue();
		assertThat(recordServices.isLogicallyDeletable(period666.getWrappedRecord(), admin)).isTrue();

		CopyRetentionRule principal42_666_T = copyBuilder.newPrincipal(asList(rm.PA()))
				.setActiveRetentionPeriod(RetentionPeriod.variable(period42))
				.setSemiActiveRetentionPeriod(RetentionPeriod.variable(period666))
				.setInactiveDisposalType(DisposalType.SORT);
		CopyRetentionRule secondary2_0_D = copyBuilder.newSecondary(asList(rm.PA()), "2-0-D");
		RetentionRule rule = rm.newRetentionRule().setCode("2").setTitle("Rule #2")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal42_666_T, secondary2_0_D));
		recordServices.add(rule);

		assertThat(recordServices.isLogicallyDeletable(period888.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period999.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period42.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period666.getWrappedRecord(), admin)).isFalse();

		rule.getCopyRetentionRules().get(0).setSemiActiveRetentionPeriod(RetentionPeriod.fixed(2));
		recordServices.update(rule);

		assertThat(recordServices.isLogicallyDeletable(period888.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period999.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period42.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period666.getWrappedRecord(), admin)).isTrue();

		recordServices.logicallyDelete(rule.getWrappedRecord(), admin);

		assertThat(recordServices.isLogicallyDeletable(period888.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period999.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period42.getWrappedRecord(), admin)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(period666.getWrappedRecord(), admin)).isTrue();
	}

}
