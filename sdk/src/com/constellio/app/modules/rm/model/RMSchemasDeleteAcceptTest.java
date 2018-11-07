package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMSchemasDeleteAcceptTest extends ConstellioTest {

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Test
	public void whenCallLogicallyThenPhysicallyDeletableCheckOnCategoriesThenGoodBehavior() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		User admin = records.getAdmin();

		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X100(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X110(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X120(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_X13(), admin).isTrue();

		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z100(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z110(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z111(), admin).isTrue();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z112(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z120(), admin).isFalse();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z200(), admin).isTrue();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_Z999(), admin).isTrue();
		assertThatLogicallyThenPhysicallyDeletable(records.getCategory_ZE42(), admin).isTrue();

		recordServices.logicallyDelete(records.getCategory_Z111().getWrappedRecord(), admin);
		recordServices.physicallyDelete(records.getCategory_Z111().getWrappedRecord(), admin);

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
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
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
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		User admin = records.getAdmin();

		assertThatLogicallyDeletable(records.getCategory_X(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_X100(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_X110(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_X120(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_X13(), admin).isTrue();

		assertThatLogicallyDeletable(records.getCategory_Z(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_Z100(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_Z110(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_Z110(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_Z111(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z112(), admin).isFalse();

		//Logically deletable, but not physically
		assertThatLogicallyDeletable(records.getCategory_Z120(), admin).isFalse();
		assertThatLogicallyDeletable(records.getCategory_Z200(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_Z999(), admin).isTrue();
		assertThatLogicallyDeletable(records.getCategory_ZE42(), admin).isTrue();
	}

	@Test
	public void givenUnusedAdministrativeUnitThenDeletable()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		User admin = users.adminIn(zeCollection);
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);
		//unitId_10a : 63,
		//unitId_30c : 21
		//unitId_11b : 11
		//unitId_12b : 10
		//unitId_20d : 0
		String referencedAndContainsRecordsErrorMessage = "Cette unité administrative ne peut pas être supprimée car elle est référenciée\nCette unité administrative ne peut pas être supprimée car elle contient un enregistrement\n";
		String referencedRecordErrorMessage = "Cette unité administrative ne peut pas être supprimée car elle est référenciée\n";
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit10().getWrappedRecord(), admin))).isEqualTo(referencedAndContainsRecordsErrorMessage);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit10a().getWrappedRecord(), admin))).isEqualTo(referencedAndContainsRecordsErrorMessage);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit11().getWrappedRecord(), admin))).isEqualTo(referencedAndContainsRecordsErrorMessage);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit11b().getWrappedRecord(), admin))).isEqualTo(referencedAndContainsRecordsErrorMessage);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit12().getWrappedRecord(), admin))).isEqualTo(referencedAndContainsRecordsErrorMessage);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit12b().getWrappedRecord(), admin))).isEqualTo(referencedAndContainsRecordsErrorMessage);
		assertThat(recordServices.validateLogicallyDeletable(records.getUnit12c().getWrappedRecord(), admin).isEmpty()).isTrue();

		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit20().getWrappedRecord(), admin))).isEqualTo(referencedRecordErrorMessage);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit30c().getWrappedRecord(), admin))).isEqualTo(referencedAndContainsRecordsErrorMessage);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(records.getUnit30().getWrappedRecord(), admin))).isEqualTo(referencedAndContainsRecordsErrorMessage);

		String usedInConfigsError = "Vous ne pouvez pas supprimer définitivement cet enregistrement, car il est utilisé dans les configurations\n";
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit10().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit10a().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit11().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit11b().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit12().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit12b().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);
		assertThat(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit12c().getWrappedRecord(), admin).isEmpty()).isTrue();

		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit20().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit30c().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyThenPhysicallyDeletable(records.getUnit30().getWrappedRecord(), admin))).isEqualTo(usedInConfigsError);

	}

	private org.assertj.core.api.BooleanAssert assertThatLogicallyDeletable(RecordWrapper recordWrapper,
																			User user) {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		return assertThat(recordServices.validateLogicallyDeletable(recordWrapper.getWrappedRecord(), user).isEmpty());
	}

	private org.assertj.core.api.BooleanAssert assertThatLogicallyThenPhysicallyDeletable(RecordWrapper recordWrapper,
																						  User user) {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		return assertThat(recordServices.validateLogicallyThenPhysicallyDeletable(recordWrapper.getWrappedRecord(), user).isEmpty());
	}

	@Test
	public void whenLogicallyDeletingVariableRetentionPeriodThenOnlyPossibleIfNotUsedAndNot888And999()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withFoldersAndContainersOfEveryStatus()
		);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(rm.newVariableRetentionPeriod().setCode("42").setTitle("42"));
		transaction.add(rm.newVariableRetentionPeriod().setCode("666").setTitle("666"));
		recordServices.execute(transaction);

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		VariableRetentionPeriod period888 = rm.PERIOD_888();
		VariableRetentionPeriod period999 = rm.PERIOD_999();
		VariableRetentionPeriod period42 = rm.getVariableRetentionPeriodWithCode("42");
		VariableRetentionPeriod period666 = rm.getVariableRetentionPeriodWithCode("666");

		String cannotDeleteVariableRetentionPeriodError = "Vous ne pouvez pas supprimer cette variable de période de conservation car elle a un code égale à 888 ou 999\n";
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period888.getWrappedRecord(), admin))).isEqualTo(cannotDeleteVariableRetentionPeriodError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period999.getWrappedRecord(), admin))).isEqualTo(cannotDeleteVariableRetentionPeriodError);
		assertThat(recordServices.validateLogicallyDeletable(period42.getWrappedRecord(), admin).isEmpty()).isTrue();
		assertThat(recordServices.validateLogicallyDeletable(period666.getWrappedRecord(), admin).isEmpty()).isTrue();

		CopyRetentionRule principal42_666_T = copyBuilder.newPrincipal(asList(rm.PA()))
				.setActiveRetentionPeriod(RetentionPeriod.variable(period42))
				.setSemiActiveRetentionPeriod(RetentionPeriod.variable(period666))
				.setInactiveDisposalType(DisposalType.SORT);
		CopyRetentionRule secondary2_0_D = copyBuilder.newSecondary(asList(rm.PA()), "2-0-D");
		RetentionRule rule = rm.newRetentionRule().setCode("2").setTitle("Rule #2")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal42_666_T, secondary2_0_D));
		recordServices.add(rule);

		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period888.getWrappedRecord(), admin))).isEqualTo(cannotDeleteVariableRetentionPeriodError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period999.getWrappedRecord(), admin))).isEqualTo(cannotDeleteVariableRetentionPeriodError);
		String variableRetentionRuleUserInRetentionRuleError = "Vous ne pouvez pas supprimer cette variable de période car elle est utilisé dans une règle de conservation\n";
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period42.getWrappedRecord(), admin))).isEqualTo(variableRetentionRuleUserInRetentionRuleError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period666.getWrappedRecord(), admin))).isEqualTo(variableRetentionRuleUserInRetentionRuleError);

		rule.getCopyRetentionRules().get(0).setSemiActiveRetentionPeriod(RetentionPeriod.fixed(2));
		recordServices.update(rule);

		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period888.getWrappedRecord(), admin))).isEqualTo(cannotDeleteVariableRetentionPeriodError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period999.getWrappedRecord(), admin))).isEqualTo(cannotDeleteVariableRetentionPeriodError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period42.getWrappedRecord(), admin))).isEqualTo(variableRetentionRuleUserInRetentionRuleError);
		assertThat(recordServices.validateLogicallyDeletable(period666.getWrappedRecord(), admin).isEmpty()).isTrue();

		recordServices.logicallyDelete(rule.getWrappedRecord(), admin);

		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period888.getWrappedRecord(), admin))).isEqualTo(cannotDeleteVariableRetentionPeriodError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period999.getWrappedRecord(), admin))).isEqualTo(cannotDeleteVariableRetentionPeriodError);
		assertThat(MessageUtils.getUserDisplayErrorMessage(recordServices.validateLogicallyDeletable(period42.getWrappedRecord(), admin))).isEqualTo(variableRetentionRuleUserInRetentionRuleError);
		assertThat(recordServices.validateLogicallyDeletable(period666.getWrappedRecord(), admin).isEmpty()).isTrue();
	}

}
