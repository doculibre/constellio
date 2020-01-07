package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.AllowModificationOfArchivisticStatusAndExpectedDatesChoice;
import com.constellio.app.modules.rm.model.enums.CompleteDatesWhenAddingFolderWithManualStatusChoice;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.validators.FolderValidator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.data.utils.Builder;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookRetriever;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.GetByIdCounter;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.CLOSE_DATE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVE_DEPOSITED;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVE_DESTROYED;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static com.constellio.app.modules.rm.model.validators.FolderValidator.CATEGORY_CODE;
import static com.constellio.app.modules.rm.model.validators.FolderValidator.RULE_CODE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.records.RecordId.toStringIds;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;

public class FolderAcceptanceTest extends ConstellioTest {

	Users users = new Users();

	LocalDate november4_2009 = new LocalDate(2009, 11, 4);
	LocalDate november4_2010 = new LocalDate(2010, 11, 4);

	LocalDate december12_2009 = new LocalDate(2009, 12, 12);
	LocalDate january12_2010 = new LocalDate(2010, 1, 12);
	LocalDate february16_2012 = new LocalDate(2012, 1, 12);

	LocalDate january1_2015 = new LocalDate(2015, 1, 1);
	LocalDate february2_2015 = new LocalDate(2015, 2, 1);
	LocalDate february11_2015 = new LocalDate(2015, 2, 11);
	LocalDate march31_2015 = new LocalDate(2015, 3, 31);
	LocalDate march31_2016 = new LocalDate(2016, 3, 31);
	LocalDate march31_2017 = new LocalDate(2017, 3, 31);
	LocalDate march31_2018 = new LocalDate(2018, 3, 31);
	LocalDate march31_2019 = new LocalDate(2019, 3, 31);
	LocalDate march31_2020 = new LocalDate(2020, 3, 31);
	LocalDate march31_2021 = new LocalDate(2021, 3, 31);
	LocalDate march31_2022 = new LocalDate(2022, 3, 31);
	LocalDate march31_2023 = new LocalDate(2023, 3, 31);
	LocalDate march31_2024 = new LocalDate(2024, 3, 31);
	LocalDate march31_2025 = new LocalDate(2025, 3, 31);

	LocalDate april1_2004 = new LocalDate(2004, 4, 1);
	LocalDate april1_2014 = new LocalDate(2014, 4, 1);
	LocalDate march31_2005 = new LocalDate(2005, 3, 31);
	LocalDate march31_2026 = new LocalDate(2026, 3, 31);
	LocalDate march31_2029 = new LocalDate(2029, 3, 31);
	LocalDate march31_2035 = new LocalDate(2035, 3, 31);
	LocalDate march31_2036 = new LocalDate(2036, 3, 31);
	LocalDate march31_2046 = new LocalDate(2046, 3, 31);
	LocalDate march31_2056 = new LocalDate(2056, 3, 31);
	LocalDate march31_2061 = new LocalDate(2061, 3, 31);
	LocalDate march31_2065 = new LocalDate(2065, 3, 31);
	LocalDate march31_2066 = new LocalDate(2066, 3, 31);
	LocalDate march31_2075 = new LocalDate(2075, 3, 31);

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	AuthorizationsServices authorizationsServices;

	Transaction transaction = new Transaction();

	String zeRule = "zeRule";
	String zeCategory;
	String aPrincipalAdminUnit;
	String anotherPrincipalAdminUnit;
	String aSecondaryAdminUnit;

	String PA;
	String MV;
	String MD;

	CopyType noEnteredCopyType = null;

	CopyRetentionRuleBuilder copyBuilder = new CopyRetentionRuleBuilderWithDefinedIds();

	@Before
	public void setUp()
			throws Exception {
		givenRollbackCheckDisabled();
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users)
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		zeCategory = records.categoryId_ZE42;
		aPrincipalAdminUnit = records.unitId_10a;
		anotherPrincipalAdminUnit = records.unitId_20;
		aSecondaryAdminUnit = records.unitId_30;
		PA = records.PA;
		MV = records.MV;
		MD = records.MD;

	}

	@Test
	public void givenEnforcedWhenCreateFolderWithIncompatibleRuleAndCategoryThenValidationException()
			throws Exception {

		givenConfig(RMConfigs.UNIFORM_SUBDIVISION_ENABLED, true);
		givenConfig(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER, true);
		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setCategoryEntered(records.categoryId_X);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setOpenDate(LocalDate.now());

		try {
			recordServices.add(folder);
			fail("Validation exception expected");
		} catch (RecordServicesException.ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, CATEGORY_CODE, RULE_CODE)).containsOnly(
					tuple("FolderValidator_folderCategoryMustBeRelatedToItsRule", "X", "2")
			);

		}

		givenConfig(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER, false);

		//OK
		recordServices.add(folder);
	}

	@Test
	public void givenEnforcedWhenCreateFolderWithIncompatibleRuleAndUniformSubdivisionThenValidationException()
			throws Exception {

		givenConfig(RMConfigs.UNIFORM_SUBDIVISION_ENABLED, true);
		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setCategoryEntered(records.categoryId_X);
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setOpenDate(LocalDate.now());
		folder.setUniformSubdivisionEntered(records.subdivId_1);

		try {
			recordServices.add(folder);
			fail("Validation exception expected");
		} catch (RecordServicesException.ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, CATEGORY_CODE, RULE_CODE)).containsOnly(
					tuple("FolderValidator_folderUniformSubdivisionMustBeRelatedToItsRule", "sub1", "1")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"La subdivision uniforme d'un dossier doit être liée à sa règle");
		}

		givenConfig(RMConfigs.UNIFORM_SUBDIVISION_ENABLED, false);

		//OK
		recordServices.add(folder);
	}

	@Test
	public void givenEnforcedWhenCreateFolderWithCompatibleRuleAndUniformSubdivisionThenNoValidationException()
			throws Exception {

		givenConfig(RMConfigs.UNIFORM_SUBDIVISION_ENABLED, true);
		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setCategoryEntered(records.categoryId_X);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setOpenDate(LocalDate.now());
		folder.setUniformSubdivisionEntered(records.subdivId_1);

		recordServices.add(folder);
	}

	@Test
	public void whenSaveFolderThenMetadataValuesSaved()
			throws Exception {

		Comment comment1 = new Comment("Ze message", records.getDakota_managerInA_userInB(), new LocalDateTime().minusWeeks(4));
		Comment comment2 = new Comment("An other message", records.getEdouard_managerInB_userInC(),
				new LocalDateTime().minusWeeks(1));

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setDescription("Ze description");
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setMediumTypes(Arrays.asList(PA, MV));
		folder.setUniformSubdivisionEntered(records.subdivId_2);
		folder.setOpenDate(november4_2009);
		folder.setCloseDateEntered(december12_2009);
		folder.setComments(asList(comment1, comment2));

		FolderStatus manualArchivisticStatus = FolderStatus.INACTIVE_DEPOSITED;
		LocalDate manualDepositDate = january1_2015, manualTransferDate = march31_2005, manualDestructionDate = march31_2016;
		folder.setManualArchivisticStatus(manualArchivisticStatus);
		folder.setManualExpectedDepositDate(manualDepositDate);
		folder.setManualExpectedTransferDate(manualTransferDate);
		folder.setManualExpectedDestructionDate(manualDestructionDate);

		folder = saveAndLoad(folder);

		assertThat(folder.getAdministrativeUnitEntered()).isEqualTo(records.unitId_11b);
		assertThat(folder.getDescription()).isEqualTo("Ze description");
		assertThat(folder.getUniformSubdivisionEntered()).isEqualTo(records.subdivId_2);
		assertThat(folder.getCategoryEntered()).isEqualTo(records.categoryId_X110);
		assertThat(folder.getCategoryCode()).isEqualTo(records.getCategory_X110().getCode());
		assertThat(folder.getRetentionRuleEntered()).isEqualTo(records.ruleId_2);
		assertThat(folder.getActiveRetentionCode()).isNull();
		assertThat(folder.getSemiActiveRetentionCode()).isNull();
		assertThat(folder.getRetentionRuleEntered()).isEqualTo(records.ruleId_2);

		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder.getTitle()).isEqualTo("Ze folder");
		assertThat(folder.getMediumTypes()).isEqualTo(Arrays.asList(PA, MV));
		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getComments()).isEqualTo(asList(comment1, comment2));
		assertThat(folder.hasAnalogicalMedium()).isTrue();
		assertThat(folder.hasElectronicMedium()).isFalse();
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);

		assertThat(folder.getManualArchivisticStatus()).isEqualTo(manualArchivisticStatus);
		assertThat(folder.getManualExpecteTransferdDate()).isEqualTo(manualTransferDate);
		assertThat(folder.getManualExpectedDepositDate()).isEqualTo(manualDepositDate);
		assertThat(folder.getManualExpectedDestructionDate()).isEqualTo(manualDestructionDate);

	}

	@Test
	public void whenSaveFolderThenNoQueries()
			throws Exception {

		getModelLayerFactory().getRecordsCaches().disableVolatileCache();
		GetByIdCounter getByIdCounter = new GetByIdCounter(getDataLayerFactory(), FolderAcceptanceTest.class);
		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), FolderAcceptanceTest.class);

		Comment comment1 = new Comment("Ze message", records.getDakota_managerInA_userInB(), new LocalDateTime().minusWeeks(4));
		Comment comment2 = new Comment("An other message", records.getEdouard_managerInB_userInC(),
				new LocalDateTime().minusWeeks(1));

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setDescription("Ze description");
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setMediumTypes(Arrays.asList(PA, MV));
		folder.setUniformSubdivisionEntered(records.subdivId_2);
		folder.setOpenDate(november4_2009);
		folder.setCloseDateEntered(december12_2009);
		folder.setComments(asList(comment1, comment2));

		FolderStatus manualArchivisticStatus = FolderStatus.INACTIVE_DEPOSITED;
		LocalDate manualDepositDate = january1_2015, manualTransferDate = march31_2005, manualDestructionDate = march31_2016;
		folder.setManualArchivisticStatus(manualArchivisticStatus);
		folder.setManualExpectedDepositDate(manualDepositDate);
		folder.setManualExpectedTransferDate(manualTransferDate);
		folder.setManualExpectedDestructionDate(manualDestructionDate);

		recordServices.add(folder.getWrappedRecord());

		assertThat(folder.getAdministrativeUnitEntered()).isEqualTo(records.unitId_11b);
		assertThat(folder.getDescription()).isEqualTo("Ze description");
		assertThat(folder.getUniformSubdivisionEntered()).isEqualTo(records.subdivId_2);
		assertThat(folder.getCategoryEntered()).isEqualTo(records.categoryId_X110);
		assertThat(folder.getCategoryCode()).isEqualTo(records.getCategory_X110().getCode());
		assertThat(folder.getRetentionRuleEntered()).isEqualTo(records.ruleId_2);
		assertThat(folder.getActiveRetentionCode()).isNull();
		assertThat(folder.getSemiActiveRetentionCode()).isNull();
		assertThat(folder.getRetentionRuleEntered()).isEqualTo(records.ruleId_2);

		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder.getTitle()).isEqualTo("Ze folder");
		assertThat(folder.getMediumTypes()).isEqualTo(Arrays.asList(PA, MV));
		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getComments()).isEqualTo(asList(comment1, comment2));
		assertThat(folder.hasAnalogicalMedium()).isTrue();
		assertThat(folder.hasElectronicMedium()).isFalse();
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);

		assertThat(folder.getManualArchivisticStatus()).isEqualTo(manualArchivisticStatus);
		assertThat(folder.getManualExpecteTransferdDate()).isEqualTo(manualTransferDate);
		assertThat(folder.getManualExpectedDepositDate()).isEqualTo(manualDepositDate);
		assertThat(folder.getManualExpectedDestructionDate()).isEqualTo(manualDestructionDate);

		recordServices.add(folder.setDescription("new description").getWrappedRecord());

		getByIdCounter.assertCalledIds().isEmpty();
		assertThat(queryCounter.newQueryCalls()).isZero();

	}

	@Test
	public void givenChildFolderWhenChangingEnteredValuesThenSetBackToNullBeforeSave()
			throws Exception {

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setDescription("Ze description");
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setMediumTypes(Arrays.asList(PA, MV));
		folder.setUniformSubdivisionEntered(records.subdivId_2);
		folder.setOpenDate(november4_2009);
		folder.setCloseDateEntered(december12_2009);

		folder = saveAndLoad(folder);

		Folder childFolder = rm.newFolder();
		childFolder.setParentFolder(folder);
		childFolder.setOpenDate(november4_2009);
		childFolder.setTitle("Ze child folder");

		childFolder = saveAndLoad(childFolder);

		childFolder.setAdministrativeUnitEntered(records.unitId_10);
		childFolder.setCategoryEntered(records.categoryId_X);
		childFolder.setRetentionRuleEntered(records.ruleId_3);
		childFolder.setCopyStatusEntered(CopyType.SECONDARY);

		childFolder = saveAndLoad(childFolder);

		assertThat(childFolder.getAdministrativeUnitEntered()).isNull();
		assertThat(childFolder.getCategoryEntered()).isNull();
		assertThat(childFolder.getRetentionRuleEntered()).isNull();
		assertThat(childFolder.getCopyStatusEntered()).isNull();
	}


	@Test
	public void givenFolderChildFolderAndDocumentsThenValidIntIdsMetadatas()
			throws Exception {
		ValueListServices valueListServices = new ValueListServices(getAppLayerFactory(), zeCollection);
		Taxonomy taxonomy = valueListServices.createTaxonomy(asMap(Language.French, "test"), false);

		MetadataSchema customTaxonomy = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(taxonomy.getSchemaTypes().get(0)).getDefaultSchema();

		Transaction tx = new Transaction();

		tx.add(recordServices.newRecordWithSchema(customTaxonomy, "v1").set(CODE, "v1").set(TITLE, "valeur 1"));
		tx.add(recordServices.newRecordWithSchema(customTaxonomy, "v2").set(CODE, "v2").set(TITLE, "valeur 2"));
		tx.add(recordServices.newRecordWithSchema(customTaxonomy, "v2a").set(CODE, "v2a").set(TITLE, "valeur 2a")
				.set(customTaxonomy.get(HierarchicalValueListItem.PARENT), "v2"));
		tx.add(recordServices.newRecordWithSchema(customTaxonomy, "v3").set(CODE, "v3").set(TITLE, "valeur 3"));
		execute(tx);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().create("taxo")
						.defineTaxonomyRelationshipToType(types.getSchemaType(taxonomy.getSchemaTypes().get(0)));
			}
		});

		Metadata folderCustomTaxoMetadata = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().getMetadata("taxo");

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setDescription("Ze description");
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setMediumTypes(Arrays.asList(PA, MV));
		folder.setUniformSubdivisionEntered(records.subdivId_2);
		folder.setOpenDate(november4_2009);
		folder.setCloseDateEntered(december12_2009);
		folder.set(folderCustomTaxoMetadata, "v2a");
		folder = saveAndLoad(folder);

		Folder childFolder = rm.newFolder();
		childFolder.setParentFolder(folder);
		childFolder.setOpenDate(november4_2009);
		childFolder.setTitle("Ze child folder");
		childFolder.set(folderCustomTaxoMetadata, "v3");
		childFolder = saveAndLoad(childFolder);

		Folder childChildFolder = rm.newFolder();
		childChildFolder.setParentFolder(childFolder);
		childChildFolder.setOpenDate(november4_2009);
		childChildFolder.setTitle("Ze child child folder");
		childChildFolder = saveAndLoad(childChildFolder);

		Document childFolderDoc = rm.newDocumentWithId("childFolderDoc");
		childFolderDoc.setFolder(childFolder);
		childFolderDoc.setTitle("Ze child child folder");
		recordServices.add(childFolderDoc);

		//SECONDARY_CONCEPTS_INT_IDS + PRINCIPALS_ANCESTORS_INT_IDS => PATH_PARTS

		assertThat(toStringIds(folder.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10);
		assertThat(toStringIds(folder.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))).containsOnly(
				records.categoryId_X110, records.categoryId_X100, records.categoryId_X, "v2a", "v2");
		assertThat(toStringIds(folder.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10);
		assertThat(toStringIds(folder.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId());
		assertThat(folder.getList(Schemas.ATTACHED_ANCESTORS)).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId());


		assertThat(toStringIds(childFolder.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10);
		assertThat(toStringIds(childFolder.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))).containsOnly(
				records.categoryId_X110, records.categoryId_X100, records.categoryId_X, "v2a", "v2", "v3");
		assertThat(toStringIds(childFolder.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId());
		assertThat(toStringIds(childFolder.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(), childFolder.getId());
		assertThat(childFolder.getList(Schemas.ATTACHED_ANCESTORS)).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(), childFolder.getId());


		assertThat(toStringIds(childChildFolder.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10);
		assertThat(toStringIds(childChildFolder.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))).containsOnly(
				records.categoryId_X110, records.categoryId_X100, records.categoryId_X, "v2a", "v2", "v3");
		assertThat(toStringIds(childChildFolder.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(), childFolder.getId());
		assertThat(toStringIds(childChildFolder.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(),
				childFolder.getId(), childFolder.getId(), childChildFolder.getId());
		assertThat(childChildFolder.getList(Schemas.ATTACHED_ANCESTORS)).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(),
				childFolder.getId(), childFolder.getId(), childChildFolder.getId());

		assertThat(toStringIds(childFolderDoc.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10);
		assertThat(toStringIds(childFolderDoc.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))).containsOnly(
				records.categoryId_X110, records.categoryId_X100, records.categoryId_X, "v2a", "v2", "v3");
		assertThat(toStringIds(childFolderDoc.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(), childFolder.getId());
		assertThat(toStringIds(childFolderDoc.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(),
				childFolder.getId(), childFolder.getId(), childFolderDoc.getId());
		assertThat(childFolderDoc.getList(Schemas.ATTACHED_ANCESTORS)).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(),
				childFolder.getId(), childFolder.getId(), childFolderDoc.getId());


		new AuthorizationsServices(getModelLayerFactory()).detach(childFolder);
		recordServices.refresh(childFolder, childChildFolder, childFolderDoc);

		assertThat(toStringIds(childFolder.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10);
		assertThat(toStringIds(childFolder.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))).containsOnly(
				records.categoryId_X110, records.categoryId_X100, records.categoryId_X, "v2a", "v2", "v3");
		assertThat(toStringIds(childFolder.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId());
		assertThat(toStringIds(childFolder.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS))).containsOnly(childFolder.getId());
		assertThat(childFolder.getList(Schemas.ATTACHED_ANCESTORS)).containsOnly(childFolder.getId(), "-" + folder.getId());

		assertThat(toStringIds(childChildFolder.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10);
		assertThat(toStringIds(childChildFolder.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))).containsOnly(
				records.categoryId_X110, records.categoryId_X100, records.categoryId_X, "v2a", "v2", "v3");
		assertThat(toStringIds(childChildFolder.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(), childFolder.getId());
		assertThat(toStringIds(childChildFolder.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS))).containsOnly(
				childFolder.getId(), childChildFolder.getId());
		assertThat(childChildFolder.getList(Schemas.ATTACHED_ANCESTORS)).containsOnly(
				childFolder.getId(), childChildFolder.getId(), "-" + folder.getId());

		assertThat(toStringIds(childFolderDoc.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10);
		assertThat(toStringIds(childFolderDoc.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))).containsOnly(
				records.categoryId_X110, records.categoryId_X100, records.categoryId_X, "v2a", "v2", "v3");
		assertThat(toStringIds(childFolderDoc.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))).containsOnly(
				records.unitId_11b, records.unitId_11, records.unitId_10, folder.getId(), childFolder.getId());
		assertThat(toStringIds(childFolderDoc.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS))).containsOnly(
				childFolder.getId(), childFolderDoc.getId());
		assertThat(childFolderDoc.getList(Schemas.ATTACHED_ANCESTORS)).containsOnly(
				childFolder.getId(), childFolderDoc.getId(), "-" + folder.getId());

	}

	@Test
	public void givenParentFolderHaveAuthThenHookCountersOkForUsers()
			throws Exception {

		TaxonomyRecordsHookRetriever retriever = getModelLayerFactory().getTaxonomyRecordsHookRetriever(zeCollection);
		assertThat(retriever.hasUserAccessToSomethingInPrincipalConcept(
				users.robinIn(zeCollection), records.getUnit10().getWrappedRecord(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInPrincipalConcept(
				users.robinIn(zeCollection), records.getUnit10a().getWrappedRecord(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X().getWrappedRecordId(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X110().getWrappedRecordId(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X120().getWrappedRecordId(), false, false)).isFalse();

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X120);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setOpenDate(november4_2009);
		recordServices.add(folder);

		Folder childFolder = rm.newFolderWithId("zeFolder").setTitle("Folder")
				.setParentFolder(folder).setOpenDate(LocalDate.now());
		recordServices.add(childFolder);

		authorizationsServices.add(authorizationForUsers(users.robinIn(zeCollection))
				.on(folder).givingReadAccess(), users.adminIn(zeCollection));

		assertThat(retriever.hasUserAccessToSomethingInPrincipalConcept(
				users.robinIn(zeCollection), records.getUnit10().getWrappedRecord(), false, false)).isTrue();
		assertThat(retriever.hasUserAccessToSomethingInPrincipalConcept(
				users.robinIn(zeCollection), records.getUnit10a().getWrappedRecord(), false, false)).isTrue();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X().getWrappedRecordId(), false, false)).isTrue();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X110().getWrappedRecordId(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X120().getWrappedRecordId(), false, false)).isTrue();

	}

	@Test
	public void givenChildFolderHaveAuthThenHookCountersOkForUsers()
			throws Exception {

		TaxonomyRecordsHookRetriever retriever = getModelLayerFactory().getTaxonomyRecordsHookRetriever(zeCollection);
		assertThat(retriever.hasUserAccessToSomethingInPrincipalConcept(
				users.robinIn(zeCollection), records.getUnit10().getWrappedRecord(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInPrincipalConcept(
				users.robinIn(zeCollection), records.getUnit10a().getWrappedRecord(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X().getWrappedRecordId(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X110().getWrappedRecordId(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X120().getWrappedRecordId(), false, false)).isFalse();

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X120);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setOpenDate(november4_2009);
		recordServices.add(folder);

		Folder childFolder = rm.newFolderWithId("zeFolder").setTitle("Folder")
				.setParentFolder(folder).setOpenDate(LocalDate.now());
		recordServices.add(childFolder);

		authorizationsServices.add(authorizationForUsers(users.robinIn(zeCollection))
				.on(childFolder).givingReadAccess(), users.adminIn(zeCollection));

		assertThat(retriever.hasUserAccessToSomethingInPrincipalConcept(
				users.robinIn(zeCollection), records.getUnit10().getWrappedRecord(), false, false)).isTrue();
		assertThat(retriever.hasUserAccessToSomethingInPrincipalConcept(
				users.robinIn(zeCollection), records.getUnit10a().getWrappedRecord(), false, false)).isTrue();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X().getWrappedRecordId(), false, false)).isTrue();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X110().getWrappedRecordId(), false, false)).isFalse();
		assertThat(retriever.hasUserAccessToSomethingInSecondaryConcept(
				users.robinIn(zeCollection), records.getCategory_X120().getWrappedRecordId(), false, false)).isTrue();

	}

	@Test
	public void givenChildFolderWhenChangingEnteredValuesThenNoQueries()
			throws Exception {

		getModelLayerFactory().getRecordsCaches().disableVolatileCache();
		GetByIdCounter getByIdCounter = new GetByIdCounter(getDataLayerFactory(), FolderAcceptanceTest.class);
		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), FolderAcceptanceTest.class);

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setDescription("Ze description");
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setMediumTypes(Arrays.asList(PA, MV));
		folder.setUniformSubdivisionEntered(records.subdivId_2);
		folder.setOpenDate(november4_2009);
		folder.setCloseDateEntered(december12_2009);

		recordServices.add(folder.getWrappedRecord());

		Folder childFolder = rm.newFolder();
		childFolder.setParentFolder(folder);
		childFolder.setOpenDate(november4_2009);
		childFolder.setTitle("Ze child folder");

		recordServices.add(childFolder.getWrappedRecord());

		childFolder.setAdministrativeUnitEntered(records.unitId_10);
		childFolder.setCategoryEntered(records.categoryId_X);
		childFolder.setRetentionRuleEntered(records.ruleId_3);
		childFolder.setCopyStatusEntered(CopyType.SECONDARY);

		recordServices.update(childFolder.getWrappedRecord());

		assertThat(childFolder.getAdministrativeUnitEntered()).isNull();
		assertThat(childFolder.getCategoryEntered()).isNull();
		assertThat(childFolder.getRetentionRuleEntered()).isNull();
		assertThat(childFolder.getCopyStatusEntered()).isNull();

		getByIdCounter.assertCalledIds().isEmpty();
		assertThat(queryCounter.newQueryCalls()).isZero();
	}

	@Test
	public void givenFolderWithFormCreatedModifiedByOnInfosThenPersisted()
			throws RecordServicesException {

		LocalDateTime dateTime1 = new LocalDateTime().plusDays(1);
		LocalDateTime dateTime2 = dateTime1.plusDays(2);

		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009)
				.setFormCreatedBy(records.getBob_userInAC().getId())
				.setFormCreatedOn(dateTime1)
				.setFormModifiedBy(records.getCharles_userInA().getId())
				.setFormModifiedOn(dateTime2));

		assertThat(folder.getFormCreatedBy()).isEqualTo(records.getBob_userInAC().getId());
		assertThat(folder.getFormCreatedOn()).isEqualTo(dateTime1);
		assertThat(folder.getFormModifiedBy()).isEqualTo(records.getCharles_userInA().getId());
		assertThat(folder.getFormModifiedOn()).isEqualTo(dateTime2);
	}

	@Test
	public void givenFolderWithoutTransferDisposalAndDestructionDatesThenActive()
			throws RecordServicesException {
		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009));

		assertThat(folder.hasAnalogicalMedium()).isTrue();
		assertThat(folder.hasElectronicMedium()).isTrue();
		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);
		assertThat(folder.getArchivisticStatus()).isEqualTo(ACTIVE);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
	}

	@Test
	public void givenFolderWithTransferDateAndWithoutDestructionOrDepositThenSemiActive()
			throws Exception {

		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009)
				.setActualTransferDate(january12_2010));

		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);
		assertThat(folder.getArchivisticStatus()).isEqualTo(SEMI_ACTIVE);
		assertThat(folder.getActualTransferDate()).isEqualTo(january12_2010);
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
	}

	@Test
	public void givenFolderWithDepositDateThenInactive()
			throws Exception {

		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009)
				.setActualTransferDate(january12_2010)
				.setActualDepositDate(february16_2012));

		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);
		assertThat(folder.getArchivisticStatus()).isEqualTo(INACTIVE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isEqualTo(january12_2010);
		assertThat(folder.getActualDepositDate()).isEqualTo(february16_2012);
		assertThat(folder.getActualDestructionDate()).isNull();
	}

	@Test
	public void givenFolderWithDestructionDateThenInactive()
			throws Exception {

		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009)
				.setActualTransferDate(january12_2010)
				.setActualDestructionDate(february16_2012));

		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);
		assertThat(folder.getArchivisticStatus()).isEqualTo(INACTIVE_DESTROYED);
		assertThat(folder.getActualTransferDate()).isEqualTo(january12_2010);
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isEqualTo(february16_2012);
	}

	@Test
	//Tested on IntelliGID 4!
	public void givenPrincipalFolderWithTwoMediumTypesAndYearEndInInsufficientPeriodThenHasValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("2-2-C", PA), principal("5-5-D", MD),
				secondary("1-0-D", MD, PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.hasAnalogicalMedium()).isTrue();
		assertThat(folder.hasElectronicMedium()).isTrue();
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2016);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("2-2-C", PA), principal("5-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("2-2-C", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2018, march31_2021);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(null, march31_2026);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2020, null);
		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2018);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(null);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2020);

	}

	@Test
	public void givenValidEnteredCopyRetentionRuleThenUsedForDatesCalculation()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);

		CopyRetentionRule principal_2_2_C = principal("2-2-C", PA);
		CopyRetentionRule principal_5_5_D = principal("5-5-D", MD);
		CopyRetentionRule secondary_1_0_D = secondary("1-0-D", MD, PA);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal_2_2_C, principal_5_5_D, secondary_1_0_D);

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMainCopyRuleEntered(principal_5_5_D.getId())
				.setMediumTypes(MD, PA));

		assertThat(folder.hasAnalogicalMedium()).isTrue();
		assertThat(folder.hasElectronicMedium()).isTrue();
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2016);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("2-2-C", PA), principal("5-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("5-5-D", MD));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2018, march31_2021);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(null, march31_2026);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2020, null);
		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2021);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2026);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(null);

	}

	@Test
	public void givenInvalidEnteredCopyRetentionRuleThenUsedNearestCopyForDatesCalculation()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);

		CopyRetentionRule principal_2_2_C = principal("2-2-C", PA);
		CopyRetentionRule principal_5_5_D = principal("5-5-D", MD);
		CopyRetentionRule secondary_1_0_D = secondary("1-0-D", MD, PA);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal_2_2_C, principal_5_5_D, secondary_1_0_D);

		try {
			saveAndLoad(principalFolderWithZeRule()
					.setOpenDate(february2_2015)
					.setMainCopyRuleEntered(secondary_1_0_D.getId())
					.setMediumTypes(MD, PA));
		} catch (RecordServicesException.ValidationException e) {
			List<ValidationError> validationErrors = e.getErrors().getValidationErrors();
			assertThat(validationErrors.size()).isEqualTo(1);

			ValidationError validationError = validationErrors.get(0);
			assertThat(validationError.getCode().endsWith(FolderValidator.FOLDER_INVALID_COPY_RETENTION_RULE));

			Map<String, Object> expectedParams = new HashMap<>();
			expectedParams.put("ruleCode", "Ze rule");
			expectedParams.put("schemaCode", "folder_default");
			expectedParams.put("mainCopyRule", secondary_1_0_D.getId());
			assertThat(validationError.getParameters()).isEqualTo(expectedParams);
			assertThat(frenchMessages(e.getErrors())).containsOnly("Le délai sélectionné doit être principal");
		}
	}

	@Test
	public void givenCustomFolderWhenModifyTaxonomyWithCopiedMetadatasThenReindexed()
			throws Exception {

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("customFolder");
				types.getSchema(Folder.DEFAULT_SCHEMA).create("zeCalculatedMetadata").setType(STRING)
						.defineDataEntry().asCalculated(ZeCategoryCodeCalculator.class);
			}
		});

		FolderType folderType = rm.newFolderType().setCode("ze type").setTitle("ze type").setLinkedSchema("customFolder");
		recordServices.add(folderType);

		Folder folder = rm.newFolderWithType(folderType)
				.setTitle("Ze custom folder")
				.setOpenDate(new LocalDate())
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X13)
				.setRetentionRuleEntered(records.ruleId_1);
		recordServices.add(folder);

		assertThatRecord(rm.getFolder(folder.getId()))
				.hasMetadata(Folder.CATEGORY_CODE, "X13")
				.hasMetadata(Folder.ADMINISTRATIVE_UNIT_CODE, "10A")
				.hasMetadata("zeCalculatedMetadata", "Ze ultimate X13");

		recordServices.update(rm.getCategoryWithCode("X13").setCode("X-13"));
		waitForBatchProcess();

		assertThatRecord(rm.getFolder(folder.getId()))
				.hasMetadata(Folder.CATEGORY_CODE, "X-13")
				.hasMetadata("zeCalculatedMetadata", "Ze ultimate X-13");

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenPrincipalFolderWithTwoMediumTypesAndYearEndInSufficientPeriodThenHasValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("5-5-D", MD), principal("2-2-C", PA),
				secondary("1-0-D", MD, PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(january1_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(january1_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2015);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("5-5-D", MD), principal("2-2-C", PA));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("2-2-C", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2020, march31_2017);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2025, null);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(null, march31_2019);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2017);
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2019);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenSemiActiveFoldersThenHasValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("888-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(april1_2004)
				.setActualTransferDate(april1_2014)
				.setMediumTypes(PA, MD));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(april1_2004);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2005);
		assertThat(folder.getActualTransferDate()).isEqualTo(april1_2014);
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("888-5-T", PA), principal("888-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("888-5-T", PA));
		assertThat(folder.getActiveRetentionCode()).isEqualTo("888");
		assertThat(folder.getSemiActiveRetentionCode()).isNull();
		assertThat(folder.getCopyRulesExpectedTransferDates()).isEqualTo(asList(new LocalDate[]{null, null}));
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2020, march31_2020);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2020, null);

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2020);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2020);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveFoldersWithOpenPeriodsThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 0);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(PA, MD));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2016);
		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("888-5-T", PA), principal("888-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("888-5-T", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2016, march31_2016);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2021, march31_2021);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2021, null);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2016);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2021);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2021);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveFoldersWithOpenPeriodsAndDecommissioningDelaysThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2026);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("888-5-T", PA), principal("888-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("888-5-T", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2056, march31_2056);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2061, march31_2061);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2061, null);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2056);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2061);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2061);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveFoldersWithOpenPeriodsWithCustomNumberOfYearForCalculationThenUsedForDateCalculation()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(
				principal("888-1-T", PA).setOpenActiveRetentionPeriod(100),
				principal("888-2-T", MD).setOpenActiveRetentionPeriod(0),
				principal("888-3-T", MD).setOpenActiveRetentionPeriod(null),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2026);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).hasSize(3);
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31(2156), march31(2056), march31(2056));
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31(2157), march31(2058), march31(2059));
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31(2157), march31(2058), march31(2059));

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31(2056));
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31(2058));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31(2058));

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveSecondaryFoldersWithPeriodsAndDecommissioningDelaysThenValidCalculatedDates()
			throws Exception {
		givenDisabledAfterTestValidations();
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 30);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("3-3-T", PA), principal("888-888-D", MD),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(secondaryFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2035);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(secondary("999-0-D", PA));
		assertThat(folder.getMainCopyRule()).isEqualTo(secondary("999-0-D", PA));
		assertThat(folder.getActiveRetentionCode()).isEqualTo("999");
		assertThat(folder.getSemiActiveRetentionCode()).isNull();
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly((LocalDate) null);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2075);
		assertThat(folder.getCopyRulesExpectedDepositDates()).isEqualTo(asList(new LocalDate[]{null}));

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2075);
		assertThat(folder.getExpectedDepositDate()).isNull();

		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, -1);
		waitForBatchProcess();
		reindexIfRequired();
		recordServices.refresh(folder);

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveSecondaryFoldersWithOpenPeriodsAndDecommissioningDelaysThenValidCalculatedDates()
			throws Exception {
		givenDisabledAfterTestValidations();
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 30);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("3-3-T", PA), principal("888-888-D", MD),
				secondary("999-888-D", PA));

		Folder folder = saveAndLoad(secondaryFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2035);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(secondary("999-888-D", PA));
		assertThat(folder.getMainCopyRule()).isEqualTo(secondary("999-888-D", PA));
		assertThat(folder.getActiveRetentionCode()).isEqualTo("999");

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2065);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(date(2105, 3, 31));
		assertThat(folder.getExpectedDepositDate()).isNull();

		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, -1);
		waitForBatchProcess();
		reindexIfRequired();
		recordServices.refresh(folder);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2065);
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveSecondaryFoldersWithClosePeriodsAndDecommissioningDelaysThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 30);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("3-3-T", PA), principal("888-888-D", MD),
				secondary("10-0-D", PA));

		Folder folder = saveAndLoad(secondaryFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2025);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(secondary("10-0-D", PA));
		assertThat(folder.getMainCopyRule()).isEqualTo(secondary("10-0-D", PA));
		assertThat(folder.getSemiActiveRetentionCode()).isNull();
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly((LocalDate) null);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2035);
		assertThat(folder.getCopyRulesExpectedDepositDates()).isEqualTo(asList(new LocalDate[]{null}));

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2035);
		assertThat(folder.getExpectedDepositDate()).isNull();

		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, -1);
		waitForBatchProcess();
		recordServices.refresh(folder);

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2035);
		assertThat(folder.getExpectedDepositDate()).isNull();

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveFoldersWithOpenPeriodsAndDisabledDecommissioningCalculationThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, -1);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, -1);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, -1);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, -1);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setCloseDateEntered(february11_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(february11_2015);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("888-5-T", PA), principal("888-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("888-5-T", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).isEqualTo(asList(new LocalDate[]{null, null}));
		assertThat(folder.getCopyRulesExpectedDestructionDates()).isEqualTo(asList(new LocalDate[]{null, null}));
		assertThat(folder.getCopyRulesExpectedDepositDates()).isEqualTo(asList(new LocalDate[]{null, null}));

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();

	}

	@Test
	public void givenFolderCreatedWithRuleWithoutPrincipalCopyTypeThenSecondaryEvenIfAdministrativeUnitIsInList()
			throws Exception {
		givenConfig(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED, false);
		givenRuleHasNoPrincipalCopyType(records.ruleId_1);

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setOpenDate(february2_2015);
		folder.setCloseDateEntered(february11_2015);
		folder.setMediumTypes(MD, PA);
		getModelLayerFactory().newRecordServices().add(folder);

		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.SECONDARY);

	}

	@Test
	public void givenFolderCreatedWithRuleWithoutPrincipalCopyTypeThenSecondaryEvenIfPrincipalEntered()
			throws Exception {
		givenConfig(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED, false);
		givenRuleHasNoPrincipalCopyType(records.ruleId_2);

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setOpenDate(february2_2015);
		folder.setCloseDateEntered(february11_2015);
		folder.setMediumTypes(MD, PA);
		getModelLayerFactory().newRecordServices().add(folder);

		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.SECONDARY);
	}

	@Test
	public void whenModifyingActualDatesThenStatusIsUpdated()
			throws Exception {

		final AtomicInteger folderStatusAddCounter = new AtomicInteger();
		final AtomicInteger folderStatusUpdateCounter = new AtomicInteger();

		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
				if (event.isSchemaType(Folder.SCHEMA_TYPE)) {

					folderStatusAddCounter.incrementAndGet();

				}
			}

			@Override
			public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
				if (event.isSchemaType(Folder.SCHEMA_TYPE)) {

					if (event.hasModifiedMetadata(Folder.ARCHIVISTIC_STATUS)) {
						folderStatusUpdateCounter.incrementAndGet();
					}

				}
			}
		});

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setOpenDate(february2_2015);
		folder.setMediumTypes(MD, PA);
		getModelLayerFactory().newRecordServices().add(folder);
		assertThat(folderStatusAddCounter.get()).isEqualTo(1);
		assertThat(folderStatusUpdateCounter.get()).isEqualTo(0);

		folder.setActualTransferDate(february11_2015);
		getModelLayerFactory().newRecordServices().update(folder);
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderStatusUpdateCounter.get()).isEqualTo(1);

		folder.setActualTransferDate(null);
		getModelLayerFactory().newRecordServices().update(folder);
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folderStatusUpdateCounter.get()).isEqualTo(2);

		folder.setActualTransferDate(february11_2015);
		folder.setActualDepositDate(february11_2015);
		getModelLayerFactory().newRecordServices().update(folder);
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
		assertThat(folderStatusAddCounter.get()).isEqualTo(1);
		assertThat(folderStatusUpdateCounter.get()).isEqualTo(3);
	}

	@Test
	public void givenRuleBasedOnCustomActiveMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.DATE);
			}
		});

		//Scénario #1 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata("dateA"),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", january1(2000));
		folder1.setOpenDate(january1(1999));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set("dateA", january1(2010));
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", january1(1990));
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", january1(2001));
		folder4.setOpenDate(january1(2001));

		Folder folder5 = transaction.add(folderBuilder.build());
		folder5.set("dateA", march1(2010));
		folder5.setOpenDate(march1(2000));

		Folder folder6 = transaction.add(folderBuilder.build());
		folder6.set("dateA", march1(1990));
		folder6.setOpenDate(march1(2000));

		Folder folder7 = transaction.add(folderBuilder.build());
		folder7.set("dateA", january1(1990));
		folder7.setOpenDate(march1(2000));
		folder7.setActualTransferDate(january1(1997));

		Folder folder8 = transaction.add(folderBuilder.build());
		folder8.set("dateA", march1(1990));
		folder8.setOpenDate(march1(2000));
		folder8.setActualTransferDate(january1(2010));

		Folder folder9 = transaction.add(folderBuilder.build());
		folder9.set("dateA", january1(1990));
		folder9.setOpenDate(march1(2000));
		folder9.setActualTransferDate(march1(2010));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2000));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(2005));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(2015));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(2040));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2031));

		assertThat(folder5.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder5.getExpectedTransferDate()).isEqualTo(march31(2016));
		assertThat(folder5.getExpectedDepositDate()).isEqualTo(march31(2041));

		assertThat(folder6.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder6.getExpectedTransferDate()).isEqualTo(march31(1996));
		assertThat(folder6.getExpectedDepositDate()).isEqualTo(march31(2021));

		assertThat(folder7.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder7.getExpectedTransferDate()).isNull();
		assertThat(folder7.getExpectedDepositDate()).isEqualTo(march31(2022));

		assertThat(folder8.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder8.getExpectedTransferDate()).isNull();
		assertThat(folder8.getExpectedDepositDate()).isEqualTo(march31(2035));

		assertThat(folder9.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder9.getExpectedTransferDate()).isNull();
		assertThat(folder9.getExpectedDepositDate()).isEqualTo(march31(2036));
	}

	@Test
	public void givenRuleBasedOnCustomActiveMetadataIgnoringActiveThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.DATE);
			}
		});

		//Scénario #1 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata("dateA").setIgnoreActivePeriod(true),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", january1(2000));
		folder1.setOpenDate(january1(1999));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set("dateA", january1(2010));
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", january1(1990));
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", january1(2001));
		folder4.setOpenDate(january1(2001));

		Folder folder5 = transaction.add(folderBuilder.build());
		folder5.set("dateA", march1(2010));
		folder5.setOpenDate(march1(2000));

		Folder folder6 = transaction.add(folderBuilder.build());
		folder6.set("dateA", march1(1990));
		folder6.setOpenDate(march1(2000));

		Folder folder7 = transaction.add(folderBuilder.build());
		folder7.set("dateA", january1(1990));
		folder7.setOpenDate(march1(2000));
		folder7.setActualTransferDate(january1(1997));

		Folder folder8 = transaction.add(folderBuilder.build());
		folder8.set("dateA", march1(1990));
		folder8.setOpenDate(march1(2000));
		folder8.setActualTransferDate(january1(2010));

		Folder folder9 = transaction.add(folderBuilder.build());
		folder9.set("dateA", january1(1990));
		folder9.setOpenDate(march1(2000));
		folder9.setActualTransferDate(march1(2010));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2000));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(2005));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(2015));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(2040));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2031));

		assertThat(folder5.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder5.getExpectedTransferDate()).isEqualTo(march31(2016));
		assertThat(folder5.getExpectedDepositDate()).isEqualTo(march31(2041));

		assertThat(folder6.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder6.getExpectedTransferDate()).isEqualTo(march31(1996));
		assertThat(folder6.getExpectedDepositDate()).isEqualTo(march31(2021));

		assertThat(folder7.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder7.getExpectedTransferDate()).isNull();
		assertThat(folder7.getExpectedDepositDate()).isEqualTo(march31(2022));

		assertThat(folder8.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder8.getExpectedTransferDate()).isNull();
		assertThat(folder8.getExpectedDepositDate()).isEqualTo(march31(2035));

		assertThat(folder9.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder9.getExpectedTransferDate()).isNull();
		assertThat(folder9.getExpectedDepositDate()).isEqualTo(march31(2036));
	}

	@Test
	public void givenRuleBasedOnSameActiveAndSemiActiveMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.DATE);
			}
		});

		//Scénario #2 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata("dateA")
						.setSemiActiveDateMetadata("dateA"),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", january1(2000));
		folder1.setOpenDate(january1(1999));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set("dateA", january1(2010));
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", january1(1990));
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", january1(2001));
		folder4.setOpenDate(january1(2001));

		Folder folder5 = transaction.add(folderBuilder.build());
		folder5.set("dateA", march1(2010));
		folder5.setOpenDate(january1(2000));

		Folder folder6 = transaction.add(folderBuilder.build());
		folder6.set("dateA", january1(1990));
		folder6.setOpenDate(march1(2000));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2000));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(2005));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(2015));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(2040));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2031));

		assertThat(folder5.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder5.getExpectedTransferDate()).isEqualTo(march31(2016));
		assertThat(folder5.getExpectedDepositDate()).isEqualTo(march31(2041));

		assertThat(folder6.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder6.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder6.getExpectedDepositDate()).isEqualTo(march31(2020));
	}

	@Test
	public void givenRuleBasedOnSameActiveAndSemiActiveMetadataIgnoringActivePeriodThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.DATE);
			}
		});

		//Scénario #2 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata("dateA")
						.setSemiActiveDateMetadata("dateA").setIgnoreActivePeriod(true),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", january1(2000));
		folder1.setOpenDate(january1(1999));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set("dateA", january1(2010));
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", january1(1990));
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", january1(2001));
		folder4.setOpenDate(january1(2001));

		Folder folder5 = transaction.add(folderBuilder.build());
		folder5.set("dateA", march1(2010));
		folder5.setOpenDate(january1(2000));

		Folder folder6 = transaction.add(folderBuilder.build());
		folder6.set("dateA", january1(1990));
		folder6.setOpenDate(march1(2000));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2000));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(2005));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(2015));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(2040));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2031));

		assertThat(folder5.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder5.getExpectedTransferDate()).isEqualTo(march31(2016));
		assertThat(folder5.getExpectedDepositDate()).isEqualTo(march31(2041));

		assertThat(folder6.getCloseDate()).isEqualTo(march31(2002));
		assertThat(folder6.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder6.getExpectedDepositDate()).isEqualTo(march31(2020));
	}

	@Test
	public void givenRuleBasedOnDifferentActiveAndSemiActiveMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.DATE);
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateB").setType(MetadataValueType.DATE);
			}
		});

		//Scénario #3 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata("dateA")
						.setSemiActiveDateMetadata("dateB"),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", january1(1990));
		folder1.setOpenDate(january1(2000));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set("dateA", january1(1990));
		folder2.set("dateB", january1(1960));
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", january1(1990));
		folder3.set("dateB", january1(2020));
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", january1(1990));
		folder4.set("dateB", january1(1990));
		folder4.setOpenDate(january1(2000));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(1995));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2050));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2020));

	}

	@Test
	public void givenRuleSemiActiveDateBasedOnLastPartOfTimeRangeAndActiveDateBasedOnNumberMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);
		givenConfig(RMConfigs.CALCULATED_METADATAS_BASED_ON_FIRST_TIMERANGE_PART, false);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.NUMBER);
				//types.getSchema(Folder.DEFAULT_SCHEMA).create("dateB").setType(MetadataValueType.NUMBER);
			}
		});

		//Scénario #3 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata("dateA")
						.setSemiActiveDateMetadata(Folder.TIME_RANGE),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", 1990);
		folder1.setOpenDate(january1(2000));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set("dateA", 1990);
		folder2.set(Folder.TIME_RANGE, "2000-1960");
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", 1990);
		folder3.set(Folder.TIME_RANGE, "2000-2020");
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", 1990);
		folder4.set(Folder.TIME_RANGE, "1900-1990");
		folder4.setOpenDate(january1(2000));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(1995));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2050));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2020));
	}

	@Test
	public void test()
			throws Exception {

		//		Folder folderA7 = rm.wrapFolder(recordServices.getDocumentById(records.folder_A07));
		//		assertThat(folderA7.getExpectedDepositDate()).isEqualTo(date(2007, 10, 31));
		//		assertThat(folderA7.getExpectedDepositDate()).isEqualTo(date(2007, 10, 31));

		Folder folder = rm.newFolderWithId("zeFolder").setTitle("Bouc").setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_Z112).setRetentionRuleEntered(records.ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		recordServices.update(folder);

		folder = rm.wrapFolder(recordServices.getDocumentById(folder.getId()));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2007, 12, 31));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2007, 12, 31));

	}

	@Test
	public void givenRuleSemiActiveDateBasedOnFirstPartTimeRangeAndActiveDateBasedOnNumberMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.NUMBER);
				//types.getSchema(Folder.DEFAULT_SCHEMA).create("dateB").setType(MetadataValueType.NUMBER);
			}
		});

		//Scénario #3 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata("dateA")
						.setSemiActiveDateMetadata(Folder.TIME_RANGE),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", 1990);
		folder1.setOpenDate(january1(2000));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set("dateA", 1990);
		folder2.set(Folder.TIME_RANGE, "2000-1960");
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", 1990);
		folder3.set(Folder.TIME_RANGE, "2000-2020");
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", 1990);
		folder4.set(Folder.TIME_RANGE, "1900-1990");
		folder4.setOpenDate(january1(2000));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(1995));
	}

	@Test
	public void givenRuleActiveDateBasedOnFirstPartOfTimeRangeAndSemiActiveDateBasedOnNumberMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				//types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.NUMBER);
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateB").setType(MetadataValueType.NUMBER);
			}
		});

		//Scénario #3 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata(Folder.TIME_RANGE)
						.setSemiActiveDateMetadata("dateB"),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set(Folder.TIME_RANGE, "0000-1990");
		folder1.setOpenDate(january1(2000));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set(Folder.TIME_RANGE, "1990-1990");
		folder2.set("dateB", 1960);
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set(Folder.TIME_RANGE, "1992-1990");
		folder3.set("dateB", 2020);
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set(Folder.TIME_RANGE, "1900-1990");
		folder4.set("dateB", 1990);
		folder4.setOpenDate(january1(2000));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(5));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(30));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(1995));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1997));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2050));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(1905));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2020));
	}

	@Test
	public void givenRuleActiveDateBasedOnLastPartOfTimeRangeAndSemiActiveDateBasedOnNumberMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);
		givenConfig(RMConfigs.CALCULATED_METADATAS_BASED_ON_FIRST_TIMERANGE_PART, false);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				//types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.NUMBER);
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateB").setType(MetadataValueType.NUMBER);
			}
		});

		//Scénario #3 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata(Folder.TIME_RANGE)
						.setSemiActiveDateMetadata("dateB"),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set(Folder.TIME_RANGE, "0000-1990");
		folder1.setOpenDate(january1(2000));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set(Folder.TIME_RANGE, "1990-1990");
		folder2.set("dateB", 1960);
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set(Folder.TIME_RANGE, "1992-1990");
		folder3.set("dateB", 2020);
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set(Folder.TIME_RANGE, "1900-1990");
		folder4.set("dateB", 1990);
		folder4.setOpenDate(january1(2000));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(1995));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2050));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2020));
	}

	@Test
	public void givenRuleBasedOnDifferentActiveAndSemiActiveIgnoringActiveMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.DATE);
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateB").setType(MetadataValueType.DATE);
			}
		});

		//Scénario #3 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);
		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setActiveDateMetadata("dateA")
						.setSemiActiveDateMetadata("dateB").setIgnoreActivePeriod(true),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", january1(1990));
		folder1.setOpenDate(january1(2000));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.set("dateA", january1(1990));
		folder2.set("dateB", january1(1960));
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", january1(1990));
		folder3.set("dateB", january1(2020));
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", january1(1990));
		folder4.set("dateB", january1(1990));
		folder4.setOpenDate(january1(2000));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(1995));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2045));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(1995));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2015));

	}

	@Test
	public void givenRuleBasedOnSemiActiveMetadataThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.DATE);
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateB").setType(MetadataValueType.DATE);
			}
		});

		//Scénario #4 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);

		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setSemiActiveDateMetadata("dateA"),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", january1(1990));
		folder1.setOpenDate(january1(2000));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", january1(2000));
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", january1(2010));
		folder4.setOpenDate(january1(2000));

		Folder folder5 = transaction.add(folderBuilder.build());
		folder5.set("dateA", january1(2010));
		folder5.setOpenDate(january1(2000));
		folder5.setActualTransferDate(january1(2030));

		Folder folder6 = transaction.add(folderBuilder.build());
		folder6.set("dateA", january1(2010));
		folder6.setOpenDate(january1(2000));
		folder6.setActualTransferDate(january1(2060));

		Folder folder7 = transaction.add(folderBuilder.build());
		folder7.set("dateA", january1(2005));
		folder7.setOpenDate(january1(2000));
		folder7.setActualTransferDate(january1(2030));

		Folder folder8 = transaction.add(folderBuilder.build());
		folder8.set("dateA", march1(2004));
		folder8.setOpenDate(january1(2000));
		folder8.setActualTransferDate(january1(2030));

		Folder folder9 = transaction.add(folderBuilder.build());
		folder9.set("dateA", january1(2010));
		folder9.setOpenDate(january1(2000));
		folder9.setActualTransferDate(january1(2060));
		folder9.setActualDepositDate(january1(2065));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2020));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(2031));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2040));

		assertThat(folder5.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder5.getExpectedTransferDate()).isNull();
		assertThat(folder5.getExpectedDepositDate()).isEqualTo(march31(2040));

		assertThat(folder6.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder6.getExpectedTransferDate()).isNull();
		assertThat(folder6.getExpectedDepositDate()).isEqualTo(march31(2060));

		assertThat(folder7.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder7.getExpectedTransferDate()).isNull();
		assertThat(folder7.getExpectedDepositDate()).isEqualTo(march31(2035));

		assertThat(folder8.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder8.getExpectedTransferDate()).isNull();
		assertThat(folder8.getExpectedDepositDate()).isEqualTo(march31(2035));

		assertThat(folder9.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder9.getExpectedTransferDate()).isNull();
		assertThat(folder9.getExpectedDepositDate()).isNull();
	}

	@Test
	public void givenRuleBasedOnSemiActiveMetadataIgnoringActiveThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateA").setType(MetadataValueType.DATE);
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateB").setType(MetadataValueType.DATE);
			}
		});

		//Scénario #4 : Délai “5-25-C”. Actif basée sur année financière, semi-actif laissé vide

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);

		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "5-25-C").setSemiActiveDateMetadata("dateA")
						.setIgnoreActivePeriod(true),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));

		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setMediumTypes(MD, PA);
				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.set("dateA", january1(1990));
		folder1.setOpenDate(january1(2000));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.setOpenDate(january1(2000));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.set("dateA", january1(2000));
		folder3.setOpenDate(january1(2000));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.set("dateA", january1(2010));
		folder4.setOpenDate(january1(2000));

		Folder folder5 = transaction.add(folderBuilder.build());
		folder5.set("dateA", january1(2010));
		folder5.setOpenDate(january1(2000));
		folder5.setActualTransferDate(january1(2030));

		Folder folder6 = transaction.add(folderBuilder.build());
		folder6.set("dateA", january1(2010));
		folder6.setOpenDate(january1(2000));
		folder6.setActualTransferDate(january1(2065));

		Folder folder7 = transaction.add(folderBuilder.build());
		folder7.set("dateA", january1(2005));
		folder7.setOpenDate(january1(2000));
		folder7.setActualTransferDate(january1(2030));

		Folder folder8 = transaction.add(folderBuilder.build());
		folder8.set("dateA", january1(2004));
		folder8.setOpenDate(january1(2000));
		folder8.setActualTransferDate(january1(2030));

		Folder folder9 = transaction.add(folderBuilder.build());
		folder9.set("dateA", january1(2010));
		folder9.setOpenDate(january1(2000));
		folder9.setActualTransferDate(january1(2060));
		folder9.setActualDepositDate(january1(2065));

		recordServices.execute(transaction);

		assertThat(folder1.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder1.getExpectedDepositDate()).isEqualTo(march31(2015));

		assertThat(folder2.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder2.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder2.getExpectedDepositDate()).isEqualTo(march31(2031));

		assertThat(folder3.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder3.getExpectedDepositDate()).isEqualTo(march31(2025));

		assertThat(folder4.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder4.getExpectedTransferDate()).isEqualTo(march31(2006));
		assertThat(folder4.getExpectedDepositDate()).isEqualTo(march31(2035));

		assertThat(folder5.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder5.getExpectedTransferDate()).isNull();
		assertThat(folder5.getExpectedDepositDate()).isEqualTo(march31(2035));

		assertThat(folder6.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder6.getExpectedTransferDate()).isNull();
		assertThat(folder6.getExpectedDepositDate()).isEqualTo(march31(2065));

		assertThat(folder7.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder7.getExpectedTransferDate()).isNull();
		assertThat(folder7.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder8.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder8.getExpectedTransferDate()).isNull();
		assertThat(folder8.getExpectedDepositDate()).isEqualTo(march31(2030));

		assertThat(folder9.getCloseDate()).isEqualTo(march31(2001));
		assertThat(folder9.getExpectedTransferDate()).isNull();
		assertThat(folder9.getExpectedDepositDate()).isNull();
	}

	@Test
	public void givenConcreteUseCaseThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, false);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("dateNaiss").setType(MetadataValueType.DATE);
			}
		});

		RetentionRule rule2 = rm.getRetentionRule(records.ruleId_2);

		rule2.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(records.PA), "888-75-D").setSemiActiveDateMetadata("dateNaiss")
						.setIgnoreActivePeriod(true),
				copyBuilder.newPrincipal(asList(records.MD), "75-0-D").setActiveDateMetadata("dateNaiss")
						.setIgnoreActivePeriod(true),
				copyBuilder.newSecondary(asList(records.MD), "42-42-D")
		));
		recordServices.update(rule2);

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);

				return folder;
			}
		};

		Folder folder1 = transaction.add(folderBuilder.build());
		folder1.setMediumTypes(records.PA);
		folder1.set("dateNaiss", january1(1986));
		folder1.setOpenDate(january1(1991));
		folder1.setCloseDateEntered(january1(1997));

		Folder folder2 = transaction.add(folderBuilder.build());
		folder2.setMediumTypes(records.MD);
		folder2.setOpenDate(january1(1991));
		folder2.setCloseDateEntered(january1(1997));

		Folder folder3 = transaction.add(folderBuilder.build());
		folder3.setMediumTypes(records.PA);
		folder3.set("dateNaiss", january1(1986));
		folder3.setOpenDate(january1(2000));
		folder3.setCloseDateEntered(january1(2001));

		Folder folder4 = transaction.add(folderBuilder.build());
		folder4.setMediumTypes(records.PA);
		folder4.set("dateNaiss", january1(1986));
		folder4.setOpenDate(january1(2000));

		recordServices.execute(transaction);
		//
		assertThat(folder1.getExpectedTransferDate()).isEqualTo(march31(1998));
		assertThat(folder1.getExpectedDestructionDate()).isEqualTo(march31(1986 + 75));

		assertThat(folder2.getExpectedTransferDate()).isNull();
		assertThat(folder2.getExpectedDestructionDate()).isEqualTo(march31(1997 + 75));

		assertThat(folder3.getExpectedTransferDate()).isEqualTo(march31(2002));
		assertThat(folder3.getExpectedDestructionDate()).isEqualTo(march31(1986 + 75));

		assertThat(folder4.getExpectedTransferDate()).isNull();
		assertThat(folder4.getExpectedDestructionDate()).isNull();

	}

	@Test
	public void givenMultipleRuleWithRetentionCopiesThenFolderOnlyCopiesOfSameTypeAreApplicable()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, false);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 1);

		RetentionRule rule2 = transaction.add(rm.getRetentionRule(records.ruleId_2));

		FolderType type1 = transaction.add(rm.newFolderType().setCode("type1").setTitle("Type 1"));
		FolderType type2 = transaction.add(rm.newFolderType().setCode("type2").setTitle("Type 2"));
		FolderType type3 = transaction.add(rm.newFolderType().setCode("type3").setTitle("Type 3"));

		CopyRetentionRule copy1 = copyBuilder.newPrincipal(asList(records.PA), "1-0-D").setTypeId(type1);
		CopyRetentionRule copy2 = copyBuilder.newPrincipal(asList(records.MD), "2-0-D").setTypeId(type1);
		CopyRetentionRule copy3 = copyBuilder.newPrincipal(records.PA_MD, "3-0-D").setTypeId(type1);

		CopyRetentionRule copy4 = copyBuilder.newPrincipal(asList(records.PA), "4-0-D").setTypeId(type2);
		CopyRetentionRule copy5 = copyBuilder.newPrincipal(asList(records.MD), "5-0-D").setTypeId(type2);
		CopyRetentionRule copy6 = copyBuilder.newPrincipal(records.PA_MD, "6-0-D").setTypeId(type2);

		CopyRetentionRule copy7 = copyBuilder.newPrincipal(asList(records.PA), "7-0-D");
		CopyRetentionRule copy8 = copyBuilder.newPrincipal(asList(records.MD), "8-0-D");
		CopyRetentionRule copy9 = copyBuilder.newPrincipal(records.PA_MD, "9-0-D");

		CopyRetentionRule copy10 = copyBuilder.newSecondary(asList(records.MD), "10-0-D");

		rule2.setCopyRetentionRules(asList(copy1, copy2, copy3, copy4, copy5, copy6, copy7, copy8, copy9, copy10));

		recordServices.execute(transaction);
		transaction = new Transaction();

		Builder<Folder> folderBuilder = new Builder<Folder>() {
			@Override
			public Folder build() {
				Folder folder = rm.newFolder();
				folder.setAdministrativeUnitEntered(records.unitId_10a);
				folder.setCategoryEntered(records.categoryId_X13);
				folder.setTitle("Ze folder");
				folder.setRetentionRuleEntered(records.ruleId_2);
				folder.setCopyStatusEntered(CopyType.PRINCIPAL);
				folder.setOpenDate(january1(1991));
				return folder;
			}
		};

		Folder folderType1 = transaction.add(folderBuilder.build()).setType(type1);
		Folder folderType1_PA = transaction.add(folderBuilder.build()).setType(type1).setMediumTypes(records.PA);
		Folder folderType1_MD = transaction.add(folderBuilder.build()).setType(type1).setMediumTypes(records.MD);
		Folder folderType1_PA_MD = transaction.add(folderBuilder.build()).setType(type1).setMediumTypes(records.PA_MD);

		Folder folderType2 = transaction.add(folderBuilder.build()).setType(type2);
		Folder folderType2_PA = transaction.add(folderBuilder.build()).setType(type2).setMediumTypes(records.PA);
		Folder folderType2_MD = transaction.add(folderBuilder.build()).setType(type2).setMediumTypes(records.MD);
		Folder folderType2_PA_MD = transaction.add(folderBuilder.build()).setType(type2).setMediumTypes(records.PA_MD);

		Folder folderType3 = transaction.add(folderBuilder.build()).setType(type3);
		Folder folderType3_PA = transaction.add(folderBuilder.build()).setType(type3).setMediumTypes(records.PA);
		Folder folderType3_MD = transaction.add(folderBuilder.build()).setType(type3).setMediumTypes(records.MD);
		Folder folderType3_PA_MD = transaction.add(folderBuilder.build()).setType(type3).setMediumTypes(records.PA_MD);

		Folder folderWithoutType = transaction.add(folderBuilder.build());
		Folder folderWithoutType_PA = transaction.add(folderBuilder.build()).setMediumTypes(records.PA);
		Folder folderWithoutType_MD = transaction.add(folderBuilder.build()).setMediumTypes(records.MD);
		Folder folderWithoutType_PA_MD = transaction.add(folderBuilder.build()).setMediumTypes(records.PA_MD);

		Folder secondaryFolderType1 = transaction.add(folderBuilder.build()).setType(type1).setMediumTypes(records.PA_MD)
				.setCopyStatusEntered(CopyType.SECONDARY);
		Folder secondaryFolderType2 = transaction.add(folderBuilder.build()).setType(type2).setMediumTypes(records.PA_MD)
				.setCopyStatusEntered(CopyType.SECONDARY);
		Folder secondaryFolder = transaction.add(folderBuilder.build()).setType(type1).setMediumTypes(records.PA_MD)
				.setCopyStatusEntered(CopyType.SECONDARY);

		recordServices.execute(transaction);

		assertThat(folderType1.getApplicableCopyRules()).containsOnly(copy1, copy2, copy3);
		assertThat(folderType1_PA.getApplicableCopyRules()).containsOnly(copy1, copy3);
		assertThat(folderType1_MD.getApplicableCopyRules()).containsOnly(copy2, copy3);
		assertThat(folderType1_PA_MD.getApplicableCopyRules()).containsOnly(copy1, copy2, copy3);

		assertThat(folderType2.getApplicableCopyRules()).containsOnly(copy4, copy5, copy6);
		assertThat(folderType2_PA.getApplicableCopyRules()).containsOnly(copy4, copy6);
		assertThat(folderType2_MD.getApplicableCopyRules()).containsOnly(copy5, copy6);
		assertThat(folderType2_PA_MD.getApplicableCopyRules()).containsOnly(copy4, copy5, copy6);

		assertThat(folderType3.getApplicableCopyRules()).containsOnly(copy7, copy8, copy9);
		assertThat(folderType3_PA.getApplicableCopyRules()).containsOnly(copy7, copy9);
		assertThat(folderType3_MD.getApplicableCopyRules()).containsOnly(copy8, copy9);
		assertThat(folderType3_PA_MD.getApplicableCopyRules()).containsOnly(copy7, copy8, copy9);

		assertThat(folderWithoutType.getApplicableCopyRules()).containsOnly(copy7, copy8, copy9);
		assertThat(folderWithoutType_PA.getApplicableCopyRules()).containsOnly(copy7, copy9);
		assertThat(folderWithoutType_MD.getApplicableCopyRules()).containsOnly(copy8, copy9);
		assertThat(folderWithoutType_PA_MD.getApplicableCopyRules()).containsOnly(copy7, copy8, copy9);

		assertThat(secondaryFolderType1.getApplicableCopyRules()).containsOnly(copy10);
		assertThat(secondaryFolderType2.getApplicableCopyRules()).containsOnly(copy10);
		assertThat(secondaryFolder.getApplicableCopyRules()).containsOnly(copy10);

	}

	@Test
	public void givenDisabledManualArchivisticMetadataWhenFolderSavedThenManualMetadataNotConsidered()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.DISABLED);
		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DEPOSITED)
				.setManualExpectedTransferDate(january12_2010)
				.setManualExpectedDepositDate(february16_2012)
				.setManualExpectedDestructionDate(january1_2015)
		);

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2056);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2061);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2061);
	}

	@Test
	public void givenEnabledManualArchivisticMetadataWhenFolderSavedThenManualMetadataConsidered()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);
		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DEPOSITED)
				.setManualExpectedTransferDate(january12_2010)
				.setManualExpectedDepositDate(february16_2012)
				.setManualExpectedDestructionDate(january1_2015)
		);

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(january12_2010);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(february16_2012);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(january1_2015);
	}

	@Test
	public void givenCompleteEnabledAndManualArchivisticMetadatasEnabledWhenSavingSemiActiveOrInactiveFolderWithoutTransferDateThenCalculateRealTransferDate()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.COMPLETE_DECOMMISSIONNING_DATE_WHEN_CREATING_FOLDER_WITH_MANUAL_STATUS,
				CompleteDatesWhenAddingFolderWithManualStatusChoice.ENABLED);
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getExpectedTransferDate()).isEqualTo(date(2056, 3, 31));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(date(2061, 3, 31));

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.SEMI_ACTIVE));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2056, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(date(2061, 3, 31));

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DEPOSITED));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2056, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DESTROYED));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2056, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.SEMI_ACTIVE)
				.setActualTransferDate(date(2055, 3, 31)));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2055, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2060, 3, 31));
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(date(2060, 3, 31));

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DEPOSITED));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2056, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA).setActualTransferDate(date(2055, 3, 31))
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DEPOSITED));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2055, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isEqualTo(date(2060, 3, 31));
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA).setActualTransferDate(date(2055, 3, 31))
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DEPOSITED)
				.setActualDepositDate(date(2061, 3, 31)));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2055, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DESTROYED));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2056, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DESTROYED)
				.setActualTransferDate(date(2055, 3, 31)));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2055, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isEqualTo(date(2060, 3, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DESTROYED)
				.setActualTransferDate(date(2055, 3, 31))
				.setActualDestructionDate(date(2061, 3, 31)));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
		assertThat(folder.getActualTransferDate()).isEqualTo(date(2055, 3, 31));
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();
	}

	@Test
	public void givenCompleteDisabledAndManualArchivisticMetadatasEnabledWhenSavingSemiActiveOrInactiveFolderWithoutTransferDateThenCalculateRealTransferDate()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenConfig(RMConfigs.COMPLETE_DECOMMISSIONNING_DATE_WHEN_CREATING_FOLDER_WITH_MANUAL_STATUS,
				CompleteDatesWhenAddingFolderWithManualStatusChoice.DISABLED);
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getExpectedTransferDate()).isEqualTo(date(2056, 3, 31));
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2061, 3, 31));
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(date(2061, 3, 31));

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.SEMI_ACTIVE));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getExpectedTransferDate()).isNull();

		//BAD values
		assertThat(folder.getExpectedDepositDate()).isNotNull();
		assertThat(folder.getExpectedDestructionDate()).isNotNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DEPOSITED));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();

		folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMediumTypes(MD, PA)
				.setManualArchivisticStatus(FolderStatus.INACTIVE_DESTROYED));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();
	}

	@Test
	public void givenEnabledManualArchivisticMetadataAndNullManualMetadataWhenFolderSavedThenManualMetadataNotConsidered()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);
		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA)
				.setManualArchivisticStatus(null)
				.setManualExpectedTransferDate(null)
				.setManualExpectedDepositDate(null)
				.setManualExpectedDestructionDate(null)
		);

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2056);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2061);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2061);
	}

	@Test
	public void givenRetentionRuleIsReferencingALogicallyDeletedAdministrativeUnitWhenCreatingAFolderWithThisRuleThenException()
			throws Exception {

		Transaction transaction = new Transaction();
		String id = transaction.add(rm.newAdministrativeUnit().setCode("zeUnit").setTitle("zeTitle")).getId();

		RetentionRule rule = records.getRule1();
		List<String> administrativeUnits = new ArrayList<>(rule.getAdministrativeUnits());
		administrativeUnits.add(id);
		rule.setAdministrativeUnits(administrativeUnits);

		transaction.add(rule);
		recordServices.execute(transaction);

		AdministrativeUnit unit = rm.getAdministrativeUnit(id);
		unit.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, true);
		recordServices.update(unit);

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setTitle("Ze title");
		folder.setOpenDate(new LocalDate());
		recordServices.add(folder);

		System.out.println(folder.getCategory());
	}

	@Test
	public void givenRetentionRuleWithoutSemiActivePeriodWhenCreatingAFolderWithThisRuleThenExpectedTransferDateIsNull()
			throws Exception {
		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009));

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNotNull();
	}

	@Test
	public void givenFolderHasAConfidentialRuleThenIsConfidential()
			throws Exception {

		Transaction tx = new Transaction();
		tx.add(records.getRule1().setConfidentialDocuments(true));
		Folder folder1 = tx.add(newFolderWithIdAndDefaultValues("folder1").setRetentionRuleEntered(records.ruleId_1));
		Folder folder2 = tx.add(newFolderWithIdAndDefaultValues("folder2").setRetentionRuleEntered(records.ruleId_2));
		Document document1 = tx.add(rm.newDocument().setFolder(folder1).setTitle("Doc 1"));
		Document document2 = tx.add(rm.newDocument().setFolder(folder2).setTitle("Doc 1"));
		recordServices.execute(tx);

		assertThat(folder1.isConfidential()).isTrue();
		assertThat(folder2.isConfidential()).isFalse();

		assertThat(folder1.isEssential()).isFalse();
		assertThat(folder2.isEssential()).isFalse();

		assertThat(document1.isConfidential()).isTrue();
		assertThat(document2.isConfidential()).isFalse();

		assertThat(document1.isEssential()).isFalse();
		assertThat(document2.isEssential()).isFalse();

	}

	@Test
	public void givenFolderHasAnEssentialRuleThenIsEssential()
			throws Exception {
		Transaction tx = new Transaction();
		tx.add(records.getRule1().setEssentialDocuments(true));
		Folder folder1 = tx.add(newFolderWithIdAndDefaultValues("folder1").setRetentionRuleEntered(records.ruleId_1));
		Folder folder2 = tx.add(newFolderWithIdAndDefaultValues("folder2").setRetentionRuleEntered(records.ruleId_2));
		Document document1 = tx.add(rm.newDocument().setFolder(folder1).setTitle("Doc 1"));
		Document document2 = tx.add(rm.newDocument().setFolder(folder2).setTitle("Doc 1"));
		recordServices.execute(tx);

		assertThat(folder1.isConfidential()).isFalse();
		assertThat(folder2.isConfidential()).isFalse();

		assertThat(folder1.isEssential()).isTrue();
		assertThat(folder2.isEssential()).isFalse();

		assertThat(document1.isConfidential()).isFalse();
		assertThat(document2.isConfidential()).isFalse();

		assertThat(document1.isEssential()).isTrue();
		assertThat(document2.isEssential()).isFalse();
	}

	@Test
	//Tested on IntelliGID 4!
	public void givenRetentionRuleWithTwoCopyRulesAndFolderWithManuallyEnteredCopyRuleThenOnlyTakeThatCopyRule()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 90);

		CopyRetentionRule principal22C = principal("2-2-C", PA);
		CopyRetentionRule principal55D = principal("5-5-D", PA);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal22C, principal55D, secondary("1-0-D", MD, PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015).setMainCopyRuleEntered(principal55D.getId()));

		assertThat(folder.getCloseDate()).isEqualTo(march31_2021);

	}

	@Test
	public void givenFolderHasBeenReactivatedWithoutACustomCalculationDateThenTranferedAgainThenFirstReactivationUsedForCalculation()
			throws Exception {

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setOpenDate(february2_2015);
		folder.setCloseDateEntered(february11_2015);
		folder.setMediumTypes(MD, PA);
		folder.setActualTransferDate(date(2018, 2, 11));
		folder.addReactivation(users.gandalfIn(zeCollection), date(2017, 2, 11));
		getModelLayerFactory().newRecordServices().add(folder);

		//42-5-C
		//assertThat(folder.getMainCopyRule().toString()).isEqualTo("todo");

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2023, 12, 31));

	}

	@Test
	public void givenFolderHasBeenReactivatedWithACustomCalculationDateThenTranferedAgainThenCustomDateUsedForCalculation()
			throws Exception {

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setOpenDate(february2_2015);
		folder.setCloseDateEntered(february11_2015);
		folder.setMediumTypes(MD, PA);
		folder.setActualTransferDate(date(2018, 2, 11));
		folder.addReactivation(users.gandalfIn(zeCollection), date(2017, 2, 11));
		getModelLayerFactory().newRecordServices().add(folder);

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2023, 12, 31));

	}

	@Test
	public void givenFolderHasBeenReactivatedWithoutACustomCalculationDateThenFirstReactivationUsedForCalculation()
			throws Exception {

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setOpenDate(february2_2015);
		folder.setCloseDateEntered(february11_2015);
		folder.setMediumTypes(MD, PA);
		folder.addReactivation(users.gandalfIn(zeCollection), date(2017, 2, 11));
		getModelLayerFactory().newRecordServices().add(folder);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(date(2018, 12, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2023, 12, 31));

	}

	@Test
	public void givenFolderHasBeenReactivatedWithACustomCalculationDateThenCustomDateUsedForCalculation()
			throws Exception {

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setOpenDate(february2_2015);
		folder.setCloseDateEntered(february11_2015);
		folder.setMediumTypes(MD, PA);

		folder.addReactivation(users.gandalfIn(zeCollection), date(2017, 2, 11));
		getModelLayerFactory().newRecordServices().add(folder);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(date(2018, 12, 31));
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isEqualTo(date(2023, 12, 31));

	}

	@Test
	public void givenUncalculatedOpenSemiActiveDateWhenFolderHasARuleWithAdditionnalOpenActivePeriodThenOnlyThisPeriodIsUsed()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, -1);
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);

		CopyRetentionRule principal888p3_2_C = principal("888-2-C", PA).setOpenActiveRetentionPeriod(3);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal888p3_2_C, secondary("1-0-D", MD, PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(date(2012, 1, 1))
				.setCloseDateEntered(date(2013, 1, 1))
				.setMainCopyRuleEntered(principal888p3_2_C.getId()));

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2016);

	}

	@Test
	public void givenCalculatedOpenSemiActiveDateWhenFolderHasARuleWithAdditionnalOpenActivePeriodThenBothPeriodsAreUsed()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);

		CopyRetentionRule principal888p3_2_C = principal("888-2-C", PA).setOpenActiveRetentionPeriod(3);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal888p3_2_C, secondary("1-0-D", MD, PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(date(2012, 1, 1))
				.setCloseDateEntered(date(2013, 1, 1))
				.setMainCopyRuleEntered(principal888p3_2_C.getId()));

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2017);
	}

	private void ensureValidationError(RecordServicesException e, Class<?> validatorClass, String errorCode,
									   Map<String, Object> parameters) {
		if (e instanceof ValidationException) {
			ValidationException ve = (ValidationException) e;
			ValidationErrors validationErrors = ve.getErrors();
			boolean matchingValidatorClass = false;
			boolean matchingCode = false;
			boolean matchingParameters = parameters == null;
			for (ValidationError validationError : validationErrors.getValidationErrors()) {
				if (validationError.getValidatorClass().equals(validatorClass)) {
					matchingValidatorClass = true;
				}
				if (validationError.getValidatorErrorCode().equals(errorCode)) {
					matchingCode = true;
				}
				if (parameters != null && parameters.equals(validationError.getParameters())) {
					matchingParameters = true;
				}
			}
			if (!matchingValidatorClass || !matchingCode || !matchingParameters) {
				fail();
			}
		} else {
			fail();
		}
	}

	@Test
	public void givenInvalidMainCopyRuleIdEnteredWhenSavingThenValidationFails()
			throws Exception {
		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setMainCopyRuleEntered("zeInvalidCopyRuleIdEntered");
		folder.setTitle("Ze title");
		folder.setOpenDate(new LocalDate());
		try {
			recordServices.add(folder);
			fail();
		} catch (ValidationException e) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(FolderValidator.MAIN_COPY_RULE, "zeInvalidCopyRuleIdEntered");
			parameters.put("schemaCode", Folder.DEFAULT_SCHEMA);
			parameters.put(FolderValidator.RULE_CODE, records.getRule1().getCode());
			ensureValidationError(e, FolderValidator.class, FolderValidator.FOLDER_INVALID_COPY_RETENTION_RULE, parameters);
			assertThat(frenchMessages(e.getErrors()))
					.containsOnly("Le délai sélectionné n'est pas un choix disponible selon la règle de conservation");
		}
	}

	@Test
	public void givenPrincipalCopyRuleChoosedForPrincipalFolderThenValidationError()
			throws Exception {
		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setMainCopyRuleEntered(records.getRule2().getSecondaryCopy().getId());
		folder.setTitle("Ze title");
		folder.setOpenDate(new LocalDate());
		try {
			recordServices.add(folder);
			fail();
		} catch (ValidationException e) {
			assertThat(frenchMessages(e.getErrors())).containsOnly("Le délai sélectionné doit être principal");
		}
	}

	@Test
	public void givenPrincipalCopyRuleChoosedForSecondaryFolderThenValidationError()
			throws Exception {
		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.SECONDARY);
		folder.setMainCopyRuleEntered(records.getRule2().getPrincipalCopies().get(0).getId());
		folder.setTitle("Ze title");
		folder.setOpenDate(new LocalDate());
		try {
			recordServices.add(folder);
			fail();
		} catch (ValidationException e) {
			assertThat(frenchMessages(e.getErrors())).containsOnly("Le délai sélectionné doit être secondaire");
		}
	}

	@Test
	public void givenFolderInNonCompletedAndNotDeletedTaskThenCannotDelete()
			throws RecordServicesException {
		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setOpenDate(february2_2015);
		folder.setCloseDateEntered(february11_2015);
		folder.setMediumTypes(MD, PA);
		recordServices.add(folder);

		Task task = rm.newRMTask().setLinkedFolders(asList(folder.getId())).setTitle("Task");
		recordServices.add(task);
		assertThat(recordServices.validateLogicallyDeletable(folder.getWrappedRecord(), users.adminIn(zeCollection)).isEmpty()).isFalse();

		recordServices.logicallyDelete(task.getWrappedRecord(), users.adminIn(zeCollection));
		assertThat(recordServices.validateLogicallyDeletable(folder.getWrappedRecord(), users.adminIn(zeCollection)).isEmpty()).isTrue();

		recordServices.restore(task.getWrappedRecord(), users.adminIn(zeCollection));
		assertThat(recordServices.validateLogicallyDeletable(folder.getWrappedRecord(), users.adminIn(zeCollection)).isEmpty()).isFalse();

		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		TasksSearchServices taskSearchServices = new TasksSearchServices(tasksSchemas);
		recordServices.update(task.setStatus(taskSearchServices.getFirstFinishedStatus().getId()));
		assertThat(recordServices.validateLogicallyDeletable(folder.getWrappedRecord(), users.adminIn(zeCollection)).isEmpty()).isTrue();
	}

	@Test
	public void givenNewParentAndChildFoldersThanValidatingTransactionShouldWork()
			throws Exception {

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setDescription("Ze description");
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setMediumTypes(Arrays.asList(PA, MV));
		folder.setUniformSubdivisionEntered(records.subdivId_2);
		folder.setOpenDate(november4_2009);
		folder.setCloseDateEntered(december12_2009);


		Folder childFolder = rm.newFolder();
		childFolder.setParentFolder(folder);
		childFolder.setOpenDate(november4_2009);
		childFolder.setTitle("Ze child folder");

		Transaction transaction = new Transaction();
		transaction.addAll(folder, childFolder);

		//CHANGE validateTransaction to reflect prepareTransaction
		recordServices.validateTransaction(transaction);
	}

	// -------------------------------------------------------------------------

	private LocalDate march1(int year) {
		return new LocalDate(year, 3, 1);
	}

	private LocalDate january1(int year) {
		return new LocalDate(year, 1, 1);
	}

	private void givenRuleHasNoPrincipalCopyType(String id)
			throws Exception {
		RetentionRule rule = rm.getRetentionRule(id);
		List<CopyRetentionRule> copyRules = new ArrayList<>(rule.getCopyRetentionRules());
		for (Iterator<CopyRetentionRule> iterator = copyRules.iterator(); iterator.hasNext(); ) {
			if (iterator.next().getCopyType() == CopyType.PRINCIPAL) {
				iterator.remove();
			}
		}
		rule.setCopyRetentionRules(copyRules);
		try {
			getModelLayerFactory().newRecordServices().update(rule);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

	}

	private Folder folderWithSingleCopyRule(CopyRetentionRule copyRetentionRule) {

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(copyRetentionRule, secondary("888-888-D", PA));

		if (!transaction.getRecords().isEmpty()) {
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			transaction = new Transaction();
		}

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(aPrincipalAdminUnit);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(zeRule);
		folder.setCopyStatusEntered(copyRetentionRule.getCopyType());
		return folder;
	}

	private Folder principalFolderWithZeRule() {

		if (!transaction.getRecords().isEmpty()) {
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			transaction = new Transaction();
		}

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(aPrincipalAdminUnit);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(zeRule);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		return folder;
	}

	private Folder secondaryFolderWithZeRule() {

		if (!transaction.getRecords().isEmpty()) {
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			transaction = new Transaction();
		}

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(aPrincipalAdminUnit);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(zeRule);
		folder.setCopyStatusEntered(CopyType.SECONDARY);
		return folder;
	}

	private Folder saveAndLoad(Folder folder)
			throws RecordServicesException {
		recordServices.add(folder.getWrappedRecord());
		return rm.getFolder(folder.getId());
	}

	private RetentionRule givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(CopyRetentionRule... rules) {
		return givenRetentionRule(rules).setResponsibleAdministrativeUnits(true);
	}

	private RetentionRule givenRuleWithAdminUnitsAndCopyRules(CopyRetentionRule... rules) {
		return givenRetentionRule(rules).setAdministrativeUnits(Arrays.asList(aPrincipalAdminUnit, anotherPrincipalAdminUnit));
	}

	private RetentionRule givenRetentionRule(CopyRetentionRule... rules) {
		RetentionRule retentionRule = rm.newRetentionRuleWithId(zeRule);

		retentionRule.setCode("Ze rule");
		retentionRule.setTitle("Ze rule");
		retentionRule.setApproved(true);
		//		retentionRule.setChildrenNodes(asList(zeCategory));
		retentionRule.setCopyRetentionRules(rules);

		return transaction.add(retentionRule);
	}

	private CopyRetentionRule principal(String status, String... mediumTypes) {
		return copyBuilder.newPrincipal(asList(mediumTypes), status);
	}

	private CopyRetentionRule secondary(String status, String... mediumTypes) {
		return copyBuilder.newSecondary(asList(mediumTypes), status);
	}

	private Folder newFolderWithIdAndDefaultValues(String id) {
		Folder folder = rm.newFolderWithId(id);
		folder.setAdministrativeUnitEntered(records.unitId_10a);
		folder.setCategoryEntered(records.categoryId_X13);
		folder.setRetentionRuleEntered(records.ruleId_1);
		folder.setTitle("Folder " + id);
		folder.setOpenDate(new LocalDate());
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		return folder;
	}

	private LocalDate march31(int year) {
		return new LocalDate(year, 3, 31);
	}

	public static class ZeCategoryCodeCalculator extends AbstractMetadataValueCalculator<String> {

		ReferenceDependency<String> codeParam = ReferenceDependency.toAString(Folder.CATEGORY, Category.CODE);

		@Override
		public String calculate(CalculatorParameters parameters) {
			String code = parameters.get(codeParam);
			return "Ze ultimate " + code;
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return singletonList(codeParam);
		}
	}

}
