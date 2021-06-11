package com.constellio.app.ui.framework.data;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.TestCalculatedSeparatedStructureCalculator;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.TestCalculatedSeparatedStructureFactory;
import com.constellio.model.entities.enums.AutocompleteSplitCriteria;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordTextInputDataProviderAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RecordTextInputDataProvider dataProvider;
	SystemConfigurationsManager systemConfigurationsManager;
	RecordServices recordServices;
	RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users)
				.withFoldersAndContainersOfEveryStatus());

	}

	private RecordTextInputDataProvider newDataProvider(User user, String schemaType, boolean writeAccess) {
		SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(user);
		return new RecordTextInputDataProvider(ConstellioFactories.getInstance(), sessionContext, schemaType, writeAccess);
	}

	@Test
	public void whenSearchingCategoriesThenGoodBehavior() throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Category.DEFAULT_SCHEMA).get(Category.KEYWORDS).setSchemaAutocomplete(true).setSearchable(true);
			}
		});

		Transaction transaction = new Transaction();
		transaction.add(records.getCategory_X100()).setKeywords(asList("majestueux bateaux"));
		transaction.add(records.getCategory_X13()).setKeywords(asList("magnifiques bateaux"));
		transaction.add(records.getCategory_Z100()).setKeywords(asList("extra (bateaux)"));
		transaction.add(records.getCategory_X120()).setCode("b");
		transaction.add(records.getCategory_ZE42()).setCode("Ze-42.05");
		transaction.add(records.getCategory_Z200()).setTitle("K1 manteau");
		transaction.add(records.getCategory_Z999()).setCode("PZ.90");
		transaction.add(records.getCategory_Z110()).setTitle("Dossier d'étudiants au baccalauréat");
		transaction.add(records.getCategory_Z111()).setTitle("Dossier étudiant en maîtrise");
		getModelLayerFactory().newRecordServices().execute(transaction);

		dataProvider = newDataProvider(users.adminIn(zeCollection), Category.SCHEMA_TYPE, false);
		assertThat(dataProvider.getData("X", 0, 20)).containsOnly(
				"categoryId_X", "categoryId_X100", "categoryId_X110", "categoryId_X120", "categoryId_X13"
		);

		assertThat(dataProvider.getData("X1", 0, 20)).containsOnly(
				"categoryId_X100", "categoryId_X110", "categoryId_X120", "categoryId_X13"
		);

		assertThat(dataProvider.getData("Ze", 0, 20)).containsOnly("categoryId_ZE42", "categoryId_Z");
		assertThat(dataProvider.getData("Ze-", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-4", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42.", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42.0", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42.05", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42.051", 0, 20)).isEmpty();
		assertThat(dataProvider.getData("Ze-42.04", 0, 20)).isEmpty();
		assertThat(dataProvider.getData("Ze4205", 0, 20)).isEmpty();

		assertThat(dataProvider.getData("c", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("ca", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("cât", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("cate", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("cateGor", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("cateGorY", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");

		assertThat(dataProvider.getData("b", 0, 20))
				.containsOnly("categoryId_X100", "categoryId_X13", "categoryId_X120", "categoryId_Z110", "categoryId_Z100");
		assertThat(dataProvider.getData("b", 0, 20))
				.containsOnly("categoryId_X100", "categoryId_X13", "categoryId_X120", "categoryId_Z110", "categoryId_Z100");

		assertThat(dataProvider.getData("magnifiques bateaux", 0, 20)).containsOnly("categoryId_X13");
		assertThat(dataProvider.getData("magnifiques (bateaux", 0, 20)).containsOnly("categoryId_X13");
		assertThat(dataProvider.getData("extra (bateaux", 0, 20)).containsOnly("categoryId_Z100");
		assertThat(dataProvider.getData("extra.bateaux", 0, 20)).isEmpty();
		assertThat(dataProvider.getData("extra bateaux", 0, 20)).containsOnly("categoryId_Z100");

		assertThat(dataProvider.getData("majestueux bateaux", 0, 20)).containsOnly("categoryId_X100");

		assertThat(dataProvider.getData("dossier étudiant", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
		assertThat(dataProvider.getData("P", 0, 20)).containsOnly("categoryId_Z999");
		assertThat(dataProvider.getData("PZ", 0, 20)).containsOnly("categoryId_Z999");
		assertThat(dataProvider.getData("PZ.", 0, 20)).containsOnly("categoryId_Z999");
		assertThat(dataProvider.getData("PZ.9 ", 0, 20)).isEmpty();
		assertThat(dataProvider.getData("PZ.9", 0, 20)).containsOnly("categoryId_Z999");
		assertThat(dataProvider.getData("PZ.90", 0, 20)).containsOnly("categoryId_Z999");
		assertThat(dataProvider.getData("90", 0, 20)).isEmpty();
		assertThat(dataProvider.getData(".90", 0, 20)).isEmpty();

		transaction.add(records.getCategory_Z999()).setTitle("manteau hiver");
		transaction.add(records.getCategory_Z()).setTitle("K1 hiver");
		getModelLayerFactory().newRecordServices().execute(transaction);
		assertThat(dataProvider.getData("K", 0, 20)).containsOnly("categoryId_Z200", "categoryId_Z");
		assertThat(dataProvider.getData("K1", 0, 20)).containsOnly("categoryId_Z200", "categoryId_Z");
		assertThat(dataProvider.getData("K1 ", 0, 20)).containsOnly("categoryId_Z200", "categoryId_Z");
		assertThat(dataProvider.getData("K1 m", 0, 20)).containsOnly("categoryId_Z200");
		assertThat(dataProvider.getData("K1 manteau", 0, 20)).containsOnly("categoryId_Z200");
		assertThat(dataProvider.getData("manteau", 0, 20)).containsOnly("categoryId_Z200", "categoryId_Z999");
	}

	@Test
	public void whenSearchingSecurisedItemsThenSecurityFilter()
			throws Exception {
		assertThat(newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true).getData("b", 0, 1000))
				.containsOnly("A04", "A05", "A06", "A07", "A08", "A09", "B02", "C02", "C55");

		assertThat(newDataProvider(users.charlesIn(zeCollection), Folder.SCHEMA_TYPE, true).getData("b", 0, 1000))
				.containsOnly("A04", "A05", "A06", "A07", "A08", "A09", "B02");

		assertThat(newDataProvider(users.aliceIn(zeCollection), Folder.SCHEMA_TYPE, true).getData("b", 0, 1000)).isEmpty();

		assertThat(newDataProvider(users.charlesIn(zeCollection), Folder.SCHEMA_TYPE, false).getData("b", 0, 1000))
				.containsOnly("A04", "A05", "A06", "A07", "A08", "A09", "B02");

		assertThat(newDataProvider(users.aliceIn(zeCollection), Folder.SCHEMA_TYPE, false).getData("b", 0, 1000))
				.containsOnly("A04", "A05", "A06", "A07", "A08", "A09", "B02", "C02", "C55");
	}

	@Test
	public void whenAutocompleteFieldWithCriteria() throws RecordServicesException {
		systemConfigurationsManager = getModelLayerFactory().getSystemConfigurationsManager();
		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection,
				(MetadataSchemaTypesAlteration) types -> types.getSchema(Folder.DEFAULT_SCHEMA)
						.create("separatedSequence").defineStructureFactory(TestCalculatedSeparatedStructureFactory.class)
						.defineDataEntry().asCalculated(TestCalculatedSeparatedStructureCalculator.class)
						.setSchemaAutocomplete(true).setSearchable(true));

		Folder parent = records.getFolder_A01();
		Folder children = records.getFolder_A02();

		whenAutocompleteFieldBehaviour(parent, children);
		whenAutocompleteFieldWithCriteriaSpace(parent, children);
		whenAutocompleteFieldWithCriteriaSpaceAndUnderscore(parent, children);
		whenAutocompleteFieldWithCriteriaSpaceAndApostrophe(parent, children);
		whenAutocompleteFieldWithCriteriaSpaceAndComma(parent, children);
		whenAutocompleteFieldWithCriteriaSpaceApostropheAndUnderscore(parent, children);
		whenAutocompleteFieldWithCriteriaSpaceCommaAndUnderscore(parent, children);
		whenAutocompleteFieldWithCriteriaSpaceCommaUnderscoreAndApostrophe(parent, children);
	}

	private void whenAutocompleteFieldBehaviour(Folder parent, Folder children) throws RecordServicesException {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA,
				AutocompleteSplitCriteria.SPACE);
		RecordTextInputDataProvider inputDataProvider =
				newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true);

		Folder ginoChouinardFolder = parent.setTitle("Gino Chouinard");
		Folder claudePoirierFolder = children.setTitle("Claude Poirier").setParentFolder(ginoChouinardFolder);
		recordServices.update(ginoChouinardFolder);
		recordServices.update(claudePoirierFolder);

		reindex();

		// autocomplete separated structure
		SPEQueryResponse mainStructureValue = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "Analysis of title '" + ginoChouinardFolder.getTitle() + "'", 0, 15);
		assertThatRecords(mainStructureValue.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		mainStructureValue = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "Analysis of title '" + claudePoirierFolder.getTitle() + "'", 0, 15);
		assertThatRecords(mainStructureValue.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());

		SPEQueryResponse gin = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "gin", 0, 15);
		assertThatRecords(gin.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse ginoChou = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "gino chou", 0, 15);
		assertThatRecords(ginoChou.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse chou = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "chou", 0, 15);
		assertThatRecords(chou.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle(),
						records.getFolder_C05().getTitle(), records.getFolder_C06().getTitle(),
						records.getFolder_A22().getTitle());
		SPEQueryResponse ginChou = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "gin chou", 0, 15);
		assertThat(ginChou.getRecords()).isEmpty();
		SPEQueryResponse ino = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "ino", 0, 15);
		assertThat(ino.getRecords()).isEmpty();

		SPEQueryResponse clau = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "clau", 0, 15);
		assertThatRecords(clau.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
		SPEQueryResponse claudePoir = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "claude poir", 0, 15);
		assertThatRecords(claudePoir.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
	}

	private void whenAutocompleteFieldWithCriteriaSpace(Folder parent, Folder children) throws RecordServicesException {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA,
				AutocompleteSplitCriteria.SPACE);
		RecordTextInputDataProvider inputDataProvider =
				newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true);

		Folder ginoChouinardFolder = parent.setTitle("Gino Chouinard");
		Folder claudePoirierFolder = children.setTitle("Claude Poirier").setParentFolder(ginoChouinardFolder);
		recordServices.update(ginoChouinardFolder);
		recordServices.update(claudePoirierFolder);

		reindex();

		SPEQueryResponse gino = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "gino", 0, 15);
		assertThatRecords(gino.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse ginoChouinard = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "gino chouinard", 0, 15);
		assertThatRecords(ginoChouinard.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());

		SPEQueryResponse claude = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "claude", 0, 15);
		assertThatRecords(claude.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
		SPEQueryResponse claudePoirier = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "claude poirier", 0, 15);
		assertThatRecords(claudePoirier.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
	}

	private void whenAutocompleteFieldWithCriteriaSpaceAndUnderscore(Folder parent, Folder children)
			throws RecordServicesException {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA,
				AutocompleteSplitCriteria.SPACE_AND_UNDERSCORE);
		RecordTextInputDataProvider inputDataProvider =
				newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true);

		Folder ginoChouinardFolder = parent.setTitle("Salut_Bonjour_Gino Chouinard");
		Folder claudePoirierFolder = children.setTitle("_TVA_Claude Poirier_").setParentFolder(ginoChouinardFolder);
		recordServices.update(ginoChouinardFolder);
		recordServices.update(claudePoirierFolder);

		reindex();

		SPEQueryResponse salut = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "salut", 0, 15);
		assertThatRecords(salut.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse bonjour = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "bonjour", 0, 15);
		assertThatRecords(bonjour.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse gino = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "gino", 0, 15);
		assertThatRecords(gino.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse chouinard = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "chouinard", 0, 15);
		assertThatRecords(chouinard.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse salutBonjour = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "salut bonjour", 0, 15);
		assertThatRecords(salutBonjour.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse salutGino = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "salut gino", 0, 15);
		assertThatRecords(salutGino.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse bonjourChouinard = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "bonjour chouinard", 0, 15);
		assertThatRecords(bonjourChouinard.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());
		SPEQueryResponse salutBonjourGinoChouinard = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "salut bonjour gino chouinard", 0, 15);
		assertThatRecords(salutBonjourGinoChouinard.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(ginoChouinardFolder.getTitle(), claudePoirierFolder.getTitle());

		SPEQueryResponse tva = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "tva", 0, 15);
		assertThatRecords(tva.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
		SPEQueryResponse claude = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "claude", 0, 15);
		assertThatRecords(claude.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
		SPEQueryResponse poirier = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "poirier", 0, 15);
		assertThatRecords(poirier.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
		SPEQueryResponse claudePoirier = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "claude poirier", 0, 15);
		assertThatRecords(claudePoirier.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
		SPEQueryResponse tvaClaude = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "tva claude", 0, 15);
		assertThatRecords(tvaClaude.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
		SPEQueryResponse tvaPoirier = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "tva poirier", 0, 15);
		assertThatRecords(tvaPoirier.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
		SPEQueryResponse tvaClaudePoirier = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "tva claude poirier", 0, 15);
		assertThatRecords(tvaClaudePoirier.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(claudePoirierFolder.getTitle());
	}

	private void whenAutocompleteFieldWithCriteriaSpaceAndApostrophe(Folder parent, Folder children)
			throws RecordServicesException {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA,
				AutocompleteSplitCriteria.SPACE_AND_APOSTROPHE);
		RecordTextInputDataProvider inputDataProvider =
				newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true);

		Folder parentFolder = parent.setTitle("L'hiver arrive bientôt");
		Folder childrentFolder = children.setTitle("'Vendredi est aujourd’hui'").setParentFolder(parentFolder);
		recordServices.update(parentFolder);
		recordServices.update(childrentFolder);

		reindex();

		SPEQueryResponse hiver = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hiver", 0, 15);
		assertThatRecords(hiver.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiverArriveBientot = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hiver arrive bientot", 0, 15);
		assertThatRecords(hiverArriveBientot.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse arriveBientot = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "arrive bientot", 0, 15);
		assertThatRecords(arriveBientot.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiverBientot = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hiver bientot", 0, 15);
		assertThatRecords(hiverBientot.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());

		SPEQueryResponse vendredi = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "vendredi", 0, 15);
		assertThatRecords(vendredi.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse aujourd = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "aujourd", 0, 15);
		assertThatRecords(aujourd.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse aujourdHui = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "aujourd hui", 0, 15);
		assertThatRecords(aujourdHui.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse vendrediAujourdHui = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "vendredi aujourd hui", 0, 15);
		assertThatRecords(vendrediAujourdHui.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse estHui = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "est hui", 0, 15);
		assertThatRecords(estHui.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse vendrediEstAujourdHui = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "vendredi est aujourd hui", 0, 15);
		assertThatRecords(vendrediEstAujourdHui.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
	}

	private void whenAutocompleteFieldWithCriteriaSpaceAndComma(Folder parent, Folder children)
			throws RecordServicesException {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA,
				AutocompleteSplitCriteria.SPACE_AND_COMMA);
		RecordTextInputDataProvider inputDataProvider =
				newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true);

		Folder parentFolder = parent.setTitle("Hi, I'm your father");
		Folder childrentFolder = children.setTitle("Paul Houde, le savant fou").setParentFolder(parentFolder);
		recordServices.update(parentFolder);
		recordServices.update(childrentFolder);

		reindex();

		SPEQueryResponse hiIm = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi i'm", 0, 15);
		assertThatRecords(hiIm.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiFather = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi father", 0, 15);
		assertThatRecords(hiFather.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiFatherImYour = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi father i'm your", 0, 15);
		assertThatRecords(hiFatherImYour.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse yourHi = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "your hi", 0, 15);
		assertThatRecords(yourHi.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());

		SPEQueryResponse leHoudFou = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "le houde fou", 0, 15);
		assertThatRecords(leHoudFou.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse houdePaulSavant = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "houde paul savant", 0, 15);
		assertThatRecords(houdePaulSavant.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse leSavantFouPaulHoude = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "le savant fou paul houde", 0, 15);
		assertThatRecords(leSavantFouPaulHoude.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
	}

	private void whenAutocompleteFieldWithCriteriaSpaceApostropheAndUnderscore(Folder parent, Folder children)
			throws RecordServicesException {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA,
				AutocompleteSplitCriteria.SPACE_AND_APOSTROPHE_AND_UNDERSCORE);
		RecordTextInputDataProvider inputDataProvider =
				newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true);

		Folder parentFolder = parent.setTitle("L'hiver_arrive bientôt");
		Folder childrentFolder = children.setTitle("'_Vendredi est aujourd’hui_'").setParentFolder(parentFolder);
		recordServices.update(parentFolder);
		recordServices.update(childrentFolder);

		reindex();

		SPEQueryResponse hiver = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hiver", 0, 15);
		assertThatRecords(hiver.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiverArriveBientot = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hiver arrive bientot", 0, 15);
		assertThatRecords(hiverArriveBientot.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse arriveBientot = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "arrive bientot", 0, 15);
		assertThatRecords(arriveBientot.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiverBientot = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hiver bientot", 0, 15);
		assertThatRecords(hiverBientot.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());

		SPEQueryResponse vendredi = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "vendredi", 0, 15);
		assertThatRecords(vendredi.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse aujourd = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "aujourd", 0, 15);
		assertThatRecords(aujourd.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse aujourdHui = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "aujourd hui", 0, 15);
		assertThatRecords(aujourdHui.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse vendrediAujourdHui = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "vendredi aujourd hui", 0, 15);
		assertThatRecords(vendrediAujourdHui.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse estHui = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "est hui", 0, 15);
		assertThatRecords(estHui.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse vendrediEstAujourdHui = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "vendredi est aujourd hui", 0, 15);
		assertThatRecords(vendrediEstAujourdHui.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
	}

	private void whenAutocompleteFieldWithCriteriaSpaceCommaAndUnderscore(Folder parent, Folder children)
			throws RecordServicesException {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA,
				AutocompleteSplitCriteria.SPACE_AND_COMMA_AND_UNDERSCORE);
		RecordTextInputDataProvider inputDataProvider =
				newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true);

		Folder parentFolder = parent.setTitle("Hi, I'm_your_father");
		Folder childrentFolder = children.setTitle("_Paul_Houde_, le savant fou").setParentFolder(parentFolder);
		recordServices.update(parentFolder);
		recordServices.update(childrentFolder);

		reindex();

		SPEQueryResponse hiIm = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi i'm", 0, 15);
		assertThatRecords(hiIm.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiFather = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi father", 0, 15);
		assertThatRecords(hiFather.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiFatherImYour = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi father i'm your", 0, 15);
		assertThatRecords(hiFatherImYour.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse yourHi = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "your hi", 0, 15);
		assertThatRecords(yourHi.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());

		SPEQueryResponse leHoudFou = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "le houde fou", 0, 15);
		assertThatRecords(leHoudFou.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse houdePaulSavant = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "houde paul savant", 0, 15);
		assertThatRecords(houdePaulSavant.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse leSavantFouPaulHoude = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "le savant fou paul houde", 0, 15);
		assertThatRecords(leSavantFouPaulHoude.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
	}

	private void whenAutocompleteFieldWithCriteriaSpaceCommaUnderscoreAndApostrophe(Folder parent, Folder children)
			throws RecordServicesException {
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA,
				AutocompleteSplitCriteria.SPACE_AND_COMMA_AND_UNDERSCORE_APOSTROPHE);
		RecordTextInputDataProvider inputDataProvider =
				newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true);

		Folder parentFolder = parent.setTitle("Hi, I'm_your_father");
		Folder childrentFolder = children.setTitle("_Paul_Houde_, l'savant fou").setParentFolder(parentFolder);
		recordServices.update(parentFolder);
		recordServices.update(childrentFolder);

		reindex();

		SPEQueryResponse hiIm = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi i m", 0, 15);
		assertThatRecords(hiIm.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiFather = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi father", 0, 15);
		assertThatRecords(hiFather.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse hiFatherImYour = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "hi father i m your", 0, 15);
		assertThatRecords(hiFatherImYour.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());
		SPEQueryResponse yourHi = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "your hi", 0, 15);
		assertThatRecords(yourHi.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(parentFolder.getTitle(), childrentFolder.getTitle());

		SPEQueryResponse leHoudFou = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "l houde fou", 0, 15);
		assertThatRecords(leHoudFou.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse houdePaulSavant = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "houde paul savant", 0, 15);
		assertThatRecords(houdePaulSavant.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
		SPEQueryResponse leSavantFouPaulHoude = inputDataProvider
				.searchAutocompleteField(users.adminIn(zeCollection), "l savant fou paul houde", 0, 15);
		assertThatRecords(leSavantFouPaulHoude.getRecords()).extractingMetadata(Folder.TITLE)
				.containsOnly(childrentFolder.getTitle());
	}
}
