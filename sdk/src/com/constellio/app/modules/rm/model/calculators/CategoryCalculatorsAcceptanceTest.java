package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.app.modules.rm.model.enums.RetentionRuleScope.DOCUMENTS;
import static com.constellio.app.modules.rm.model.enums.RetentionRuleScope.DOCUMENTS_AND_FOLDER;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;

public class CategoryCalculatorsAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	SearchServices searchServices;
	MetadataSchemasManager metadataSchemasManager;

	Document document;

	String w = "w";
	String w100 = "w100";
	String w110 = "w110";
	String w120 = "w120";
	String w200 = "w200";
	String w210 = "w210";
	String w220 = "w220";

	String type1 = "type1";
	String type2 = "type2";
	String type3 = "type3";
	String type4 = "type4";
	String type5 = "type5";

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		//		Transaction transaction = new Transaction();
		//
		//		recordServices.execute(transaction);

	}

	@Test
	public void givenRubricHasRuleThenAvailableForChildRubricWithoutRules()
			throws Exception {

		CopyRetentionRule principal888_1_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-1-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_2_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-2-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_3_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-3-C")
				.setTypeId(type3);
		CopyRetentionRule principal888_4_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-4-C");
		CopyRetentionRule secondary888_5_C = copyBuilder.newSecondary(TestUtils.asList(records.PA), "888-5-C");
		CopyRetentionRule principal888_8_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-8-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_9_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-9-C")
				.setTypeId(type4);
		CopyRetentionRule principal888_10_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-10-C")
				.setTypeId(type3);

		CopyRetentionRule principal888_16_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-16-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_17_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-17-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_19_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-19-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_20_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-20-C")
				.setTypeId(type5);

		CopyRetentionRule principal888_21_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-21-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_22_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-22-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_23_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-23-C")
				.setTypeId(type3);
		CopyRetentionRule principal888_24_C = copyBuilder.newPrincipal(TestUtils.asList(records.PA), "888-24-C")
				.setTypeId(type4);

		Transaction transaction = new Transaction();

		transaction.add(rm.newDocumentTypeWithId(type1).setCode("type1Code").setTitle("Ze type 1"));
		transaction.add(rm.newDocumentTypeWithId(type2).setCode("type2Code").setTitle("Ze type 2"));
		transaction.add(rm.newDocumentTypeWithId(type3).setCode("type3Code").setTitle("Ze type 3"));
		transaction.add(rm.newDocumentTypeWithId(type4).setCode("type4Code").setTitle("Ze type 4"));
		transaction.add(rm.newDocumentTypeWithId(type5).setCode("type5Code").setTitle("Ze type 5"));

		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(principal888_1_C, principal888_2_C, principal888_3_C);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_4_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_5_C);

		RetentionRule rule2 = transaction.add(rm.newRetentionRuleWithId("rule2").setCode("rule2").setTitle("rule2"));
		rule2.setScope(DOCUMENTS);
		rule2.setResponsibleAdministrativeUnits(true);
		rule2.setDocumentCopyRetentionRules(principal888_8_C, principal888_9_C);
		rule2.setPrincipalDefaultDocumentCopyRetentionRule(principal888_4_C);
		rule2.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_5_C);

		RetentionRule rule3 = transaction.add(rm.newRetentionRuleWithId("rule3").setCode("rule3").setTitle("rule3"));
		rule3.setScope(DOCUMENTS);
		rule3.setResponsibleAdministrativeUnits(true);
		rule3.setDocumentCopyRetentionRules(principal888_10_C);
		rule3.setPrincipalDefaultDocumentCopyRetentionRule(principal888_4_C);
		rule3.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_5_C);

		RetentionRule rule4 = transaction.add(rm.newRetentionRuleWithId("rule4").setCode("rule4").setTitle("rule4"));
		rule4.setScope(DOCUMENTS);
		rule4.setResponsibleAdministrativeUnits(true);
		rule4.setDocumentCopyRetentionRules(principal888_16_C, principal888_17_C);
		rule4.setPrincipalDefaultDocumentCopyRetentionRule(principal888_4_C);
		rule4.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_5_C);

		RetentionRule rule5 = transaction.add(rm.newRetentionRuleWithId("rule5").setCode("rule5").setTitle("rule5"));
		rule5.setScope(DOCUMENTS);
		rule5.setResponsibleAdministrativeUnits(true);
		rule5.setDocumentCopyRetentionRules(principal888_19_C, principal888_20_C);
		rule5.setPrincipalDefaultDocumentCopyRetentionRule(principal888_4_C);
		rule5.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_5_C);

		RetentionRule rule6 = transaction.add(rm.newRetentionRuleWithId("rule6").setCode("rule6").setTitle("rule6"));
		rule6.setScope(DOCUMENTS_AND_FOLDER);
		rule6.setResponsibleAdministrativeUnits(true);
		rule6.setDocumentCopyRetentionRules(principal888_21_C, principal888_22_C, principal888_23_C, principal888_24_C);
		rule6.setCopyRetentionRules(principal888_4_C, secondary888_5_C);

		Category w = transaction.add(rm.newCategoryWithId("w").setCode("W").setTitle("W")
				.setRetentionRules(asList(rule1, rule6)));
		Category w100 = transaction.add(rm.newCategoryWithId("w100").setCode("W-100").setTitle("W-100").setParent(w)
				.setRetentionRules(asList(rule2, rule3)));
		Category w110 = transaction.add(rm.newCategoryWithId("w110").setCode("W-110").setTitle("W-110").setParent(w100)
				.setRetentionRules(asList(rule4, rule5)));
		Category w120 = transaction.add(rm.newCategoryWithId("w120").setCode("W-120").setTitle("W-120").setParent(w100));

		recordServices.execute(transaction);

		assertThat(w.getCopyRetentionRulesOnDocumentTypes()).containsOnly(
				principal888_1_C.in("rule1", "w", 0), //type1
				principal888_2_C.in("rule1", "w", 0), //type2
				principal888_3_C.in("rule1", "w", 0)); //type3

		assertThat(w100.getCopyRetentionRulesOnDocumentTypes()).containsOnly(
				principal888_8_C.in("rule2", "w100", 1), //type1
				principal888_2_C.in("rule1", "w", 0), //type2
				principal888_10_C.in("rule3", "w100", 1), //type3
				principal888_9_C.in("rule2", "w100", 1)); //type4

		assertThat(w110.getCopyRetentionRulesOnDocumentTypes()).containsOnly(
				principal888_16_C.in("rule4", "w110", 2), //type1
				principal888_19_C.in("rule5", "w110", 2), //type1
				principal888_17_C.in("rule4", "w110", 2), //type2
				principal888_10_C.in("rule3", "w100", 1), //type3
				principal888_9_C.in("rule2", "w100", 1), //type4
				principal888_20_C.in("rule5", "w110", 2)); //type5

		assertThat(w120.getCopyRetentionRulesOnDocumentTypes()).containsOnly(
				principal888_8_C.in("rule2", "w100", 1), //type1
				principal888_2_C.in("rule1", "w", 0), //type2
				principal888_10_C.in("rule3", "w100", 1), //type3
				principal888_9_C.in("rule2", "w100", 1)); //type4
	}

}
