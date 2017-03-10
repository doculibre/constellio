package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecordWithCopyRetentionRuleParametersPresenterAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	SearchServices searchServices;
	MetadataSchemasManager metadataSchemasManager;

	Users users = new Users();
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

	@Mock RecordWithCopyRetentionRuleParametersFields fields;

	RecordWithCopyRetentionRuleParametersPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		//		Transaction transaction = new Transaction();
		//
		//		recordServices.execute(transaction);

		ConstellioFactories constellioFactories = getConstellioFactories();

		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(fields.getSessionContext()).thenReturn(sessionContext);
		when(fields.getConstellioFactories()).thenReturn(constellioFactories);

		presenter = spy(new RecordWithCopyRetentionRuleParametersPresenter(fields));

	}

	@Test
	public void givenWithQueryAndRubricHasRuleThenAvailableForChildRubricWithoutRules()
			throws Exception {

		CopyRetentionRule principal888_1_C = copyBuilder.newPrincipal(asList(records.PA), "888-1-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_2_C = copyBuilder.newPrincipal(asList(records.PA), "888-2-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_3_C = copyBuilder.newPrincipal(asList(records.PA), "888-3-C")
				.setTypeId(type3);
		CopyRetentionRule principal888_6_C = copyBuilder.newPrincipal(asList(records.MD), "888-3-C")
				.setTypeId(type3);
		CopyRetentionRule principal888_4_C = copyBuilder.newPrincipal(asList(records.PA), "888-4-C");
		CopyRetentionRule secondary888_5_C = copyBuilder.newSecondary(asList(records.PA), "888-5-C");

		CopyRetentionRule principal888_8_C = copyBuilder.newPrincipal(asList(records.PA), "888-8-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_9_C = copyBuilder.newPrincipal(asList(records.PA), "888-9-C")
				.setTypeId(type4);
		CopyRetentionRule principal888_10_C = copyBuilder.newPrincipal(asList(records.PA), "888-10-C")
				.setTypeId(type3);

		CopyRetentionRule principal888_16_C = copyBuilder.newPrincipal(asList(records.PA), "888-16-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_17_C = copyBuilder.newPrincipal(asList(records.PA), "888-17-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_19_C = copyBuilder.newPrincipal(asList(records.PA), "888-19-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_20_C = copyBuilder.newPrincipal(asList(records.PA), "888-20-C")
				.setTypeId(type5);

		CopyRetentionRule principal888_21_C = copyBuilder.newPrincipal(asList(records.PA), "888-21-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_22_C = copyBuilder.newPrincipal(asList(records.PA), "888-22-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_23_C = copyBuilder.newPrincipal(asList(records.PA), "888-23-C")
				.setTypeId(type3);
		CopyRetentionRule principal888_24_C = copyBuilder.newPrincipal(asList(records.PA), "888-24-C")
				.setTypeId(type4);

		Transaction transaction = new Transaction();

		transaction.add(rm.newFolderTypeWithId(type1).setCode("type1Code").setTitle("Ze type 1"));
		transaction.add(rm.newFolderTypeWithId(type2).setCode("type2Code").setTitle("Ze type 2"));
		transaction.add(rm.newFolderTypeWithId(type3).setCode("type3Code").setTitle("Ze type 3"));
		transaction.add(rm.newFolderTypeWithId(type4).setCode("type4Code").setTitle("Ze type 4"));
		transaction.add(rm.newFolderTypeWithId(type5).setCode("type5Code").setTitle("Ze type 5"));

		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setCopyRetentionRules(principal888_1_C, principal888_2_C, principal888_3_C, principal888_4_C, secondary888_5_C,
				principal888_6_C);

		Category w = transaction.add(rm.newCategoryWithId("w").setCode("W").setTitle("W")
				.setRetentionRules(asList(rule1)));

		Folder folderWithoutType = transaction.add(rm.newFolder().setAdministrativeUnitEntered(records.unitId_10a).setTitle("1")
				.setCategoryEntered("w").setRetentionRuleEntered(rule1)).setOpenDate(new LocalDate())
				.setCopyStatusEntered(PRINCIPAL).setMediumTypes(asList(rm.getMediumTypeByCode("PA")));

		Folder folderWithType3 = transaction.add(rm.newFolder().setAdministrativeUnitEntered(records.unitId_10a).setTitle("1")
				.setCategoryEntered("w").setRetentionRuleEntered(rule1)).setOpenDate(new LocalDate())
				.setCopyStatusEntered(PRINCIPAL).setMediumTypes(asList(rm.getMediumTypeByCode("DM"))).setType(type3);

		recordServices.execute(transaction);
		MetadataSchemaType folderSchemaType = rm.folder.schemaType();

		//Validate documents in folder
		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(type1);
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		when(fields.getQuery()).thenReturn(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(folderWithType3.getId(), folderWithoutType.getId()))));
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_1_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(type2);
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		when(fields.getQuery()).thenReturn(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(folderWithType3.getId(), folderWithoutType.getId()))));
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_2_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(type3);
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		when(fields.getQuery()).thenReturn(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(folderWithType3.getId(), folderWithoutType.getId()))));
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_3_C, principal888_6_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(type4);
		when(fields.getQuery()).thenReturn(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(folderWithType3.getId(), folderWithoutType.getId()))));
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_4_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(null);
		when(fields.getQuery()).thenReturn(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(folderWithType3.getId(), folderWithoutType.getId()))));
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOptions(presenter.toRequest())).isEmpty();

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(null);
		when(fields.getQuery()).thenReturn(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(folderWithType3.getId()))));
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_3_C, principal888_6_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(null);
		when(fields.getQuery()).thenReturn(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(folderWithoutType.getId()))));
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_4_C);

	}

	@Test
	public void givenWithIdsAndRubricHasRuleThenAvailableForChildRubricWithoutRules()
			throws Exception {

		CopyRetentionRule principal888_1_C = copyBuilder.newPrincipal(asList(records.PA), "888-1-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_2_C = copyBuilder.newPrincipal(asList(records.PA), "888-2-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_3_C = copyBuilder.newPrincipal(asList(records.PA), "888-3-C")
				.setTypeId(type3);
		CopyRetentionRule principal888_6_C = copyBuilder.newPrincipal(asList(records.MD), "888-3-C")
				.setTypeId(type3);
		CopyRetentionRule principal888_4_C = copyBuilder.newPrincipal(asList(records.PA), "888-4-C");
		CopyRetentionRule secondary888_5_C = copyBuilder.newSecondary(asList(records.PA), "888-5-C");

		CopyRetentionRule principal888_8_C = copyBuilder.newPrincipal(asList(records.PA), "888-8-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_9_C = copyBuilder.newPrincipal(asList(records.PA), "888-9-C")
				.setTypeId(type4);
		CopyRetentionRule principal888_10_C = copyBuilder.newPrincipal(asList(records.PA), "888-10-C")
				.setTypeId(type3);

		CopyRetentionRule principal888_16_C = copyBuilder.newPrincipal(asList(records.PA), "888-16-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_17_C = copyBuilder.newPrincipal(asList(records.PA), "888-17-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_19_C = copyBuilder.newPrincipal(asList(records.PA), "888-19-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_20_C = copyBuilder.newPrincipal(asList(records.PA), "888-20-C")
				.setTypeId(type5);

		CopyRetentionRule principal888_21_C = copyBuilder.newPrincipal(asList(records.PA), "888-21-C")
				.setTypeId(type1);
		CopyRetentionRule principal888_22_C = copyBuilder.newPrincipal(asList(records.PA), "888-22-C")
				.setTypeId(type2);
		CopyRetentionRule principal888_23_C = copyBuilder.newPrincipal(asList(records.PA), "888-23-C")
				.setTypeId(type3);
		CopyRetentionRule principal888_24_C = copyBuilder.newPrincipal(asList(records.PA), "888-24-C")
				.setTypeId(type4);

		Transaction transaction = new Transaction();

		transaction.add(rm.newFolderTypeWithId(type1).setCode("type1Code").setTitle("Ze type 1"));
		transaction.add(rm.newFolderTypeWithId(type2).setCode("type2Code").setTitle("Ze type 2"));
		transaction.add(rm.newFolderTypeWithId(type3).setCode("type3Code").setTitle("Ze type 3"));
		transaction.add(rm.newFolderTypeWithId(type4).setCode("type4Code").setTitle("Ze type 4"));
		transaction.add(rm.newFolderTypeWithId(type5).setCode("type5Code").setTitle("Ze type 5"));

		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setCopyRetentionRules(principal888_1_C, principal888_2_C, principal888_3_C, principal888_4_C, secondary888_5_C,
				principal888_6_C);

		Category w = transaction.add(rm.newCategoryWithId("w").setCode("W").setTitle("W")
				.setRetentionRules(asList(rule1)));

		Folder folderWithoutType = transaction.add(rm.newFolder().setAdministrativeUnitEntered(records.unitId_10a).setTitle("1")
				.setCategoryEntered("w").setRetentionRuleEntered(rule1)).setOpenDate(new LocalDate())
				.setCopyStatusEntered(PRINCIPAL).setMediumTypes(asList(rm.getMediumTypeByCode("PA")));

		Folder folderWithType3 = transaction.add(rm.newFolder().setAdministrativeUnitEntered(records.unitId_10a).setTitle("1")
				.setCategoryEntered("w").setRetentionRuleEntered(rule1)).setOpenDate(new LocalDate())
				.setCopyStatusEntered(PRINCIPAL).setMediumTypes(asList(rm.getMediumTypeByCode("DM"))).setType(type3);

		recordServices.execute(transaction);
		MetadataSchemaType folderSchemaType = rm.folder.schemaType();

		//Validate documents in folder
		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(type1);
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		when(fields.getSelectedRecords()).thenReturn(asList(folderWithType3.getId(), folderWithoutType.getId()));
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_1_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(type2);
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		when(fields.getSelectedRecords()).thenReturn(asList(folderWithType3.getId(), folderWithoutType.getId()));
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_2_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(type3);
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		when(fields.getSelectedRecords()).thenReturn(asList(folderWithType3.getId(), folderWithoutType.getId()));
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_3_C, principal888_6_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(type4);
		when(fields.getSelectedRecords()).thenReturn(asList(folderWithType3.getId(), folderWithoutType.getId()));
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_4_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(null);
		when(fields.getSelectedRecords()).thenReturn(asList(folderWithType3.getId(), folderWithoutType.getId()));
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOptions(presenter.toRequest())).isEmpty();

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(null);
		when(fields.getSelectedRecords()).thenReturn(asList(folderWithType3.getId()));
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_3_C, principal888_6_C);

		doReturn(null).when(presenter).getDependencyValue();
		when(fields.getType()).thenReturn(null);
		when(fields.getSelectedRecords()).thenReturn(asList(folderWithoutType.getId()));
		when(fields.getSchemaType()).thenReturn(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOptions(presenter.toRequest())).containsOnly(principal888_4_C);

	}
}
