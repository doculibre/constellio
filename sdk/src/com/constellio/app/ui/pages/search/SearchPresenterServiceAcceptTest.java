package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetOrderType;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SearchPresenterServiceAcceptTest extends ConstellioTest {
	LocalDateTime threeYearsAgo = new LocalDateTime().minusYears(3);

	LogicalSearchQuery allFolders;
	LogicalSearchQuery allFoldersAndDocuments;

	RMSchemasRecordsServices rm;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;
	ContentManager contentManager;
	SearchPresenterService searchPresenterService;
	Map<String, Boolean> facetStatus;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		contentManager = getModelLayerFactory().getContentManager();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		searchPresenterService = new SearchPresenterService(zeCollection, users.adminIn(zeCollection), getModelLayerFactory(), null);

		allFolders = new LogicalSearchQuery(from(rm.folderSchemaType()).returnAll());
		allFoldersAndDocuments = new LogicalSearchQuery(from(asList(rm.folderSchemaType(), rm.documentSchemaType())).returnAll());

		facetStatus = new HashMap<>();

		clearExistingFacets();
	}

	@Test
	public void givenFieldAndQueryFacetsConfiguredWhenSearchingFoldersThenReturnThoseWithValues()
			throws Exception {
		recordServices.add(rm.newFacetQuery().setOrder(0).setTitle("Type")
				.withQuery("schema_s:folder*", "Dossiers")
				.withQuery("schema_s:document*", "Documents")
				.withQuery("schema_s:containerRecord*", "Contenants"));

		recordServices.add(rm.newFacetField().setOrder(1).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations"));
		recordServices.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("copyStatus_s").setTitle("Statut d'exemplaire"));
		recordServices.add(rm.newFacetField().setOrder(3).setFieldDataStoreCode("keywords_ss").setTitle("Mots-clés"));
		recordServices.add(rm.newFacetField().setOrder(4).setFieldDataStoreCode("type_s").setTitle("Type"));
		recordServices.add(rm.newFacetQuery().setOrder(5).setTitle("Création/Modification")
				.withQuery("modifiedOn_dt:[NOW-1MONTH TO NOW]", "Modifiés les 30 derniers jours")
				.withQuery("modifiedOn_dt:[NOW-7DAY TO NOW]", "Modifiés les 7 derniers jours")
				.withQuery("createdOn_dt:[NOW-1MONTH TO NOW]", "Créés les 30 derniers jours")
				.withQuery("createdOn_dt:[NOW-7DAY TO NOW]", "Créés les 7 derniers jours"));
		recordServices.add(rm.newFacetField().setOrder(6).setFieldDataStoreCode("administrativeUnitId_s")
				.setTitle("Unités administratives"));

		List<FacetVO> facets = searchPresenterService.getFacets(allFolders, facetStatus, Locale.FRENCH);
		assertThat(facets.get(1)).has(label("Règles de conservations")).has(
				values(value(records.getRule2()), value(records.getRule4()), value(records.getRule1()),
						value(records.getRule3())));

		assertThat(facets.get(2)).has(label("Statut d'exemplaire")).has(
				values(value(CopyType.PRINCIPAL.getCode(), "Principal"), value(CopyType.SECONDARY.getCode(), "Secondaire")));

		assertThat(facets.get(4)).has(label("Unités administratives")).has(
				values(value(records.getUnit10a()), value(records.getUnit30c()), value(records.getUnit11b()),
						value(records.getUnit12b())));

	}

	@Test
	public void givenMimeTypeFieldFacetsConfiguredWhenSearchingFolderWithMicrosoftDocumentsThenReturnRightLabelForValue()
			throws Exception {
		Folder folder = rm.newFolderWithId("1999").setTitle("Microsoft Documents")
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X13).setRetentionRuleEntered(records.ruleId_1)
				.setMediumTypes(rm.PA()).setOpenDate(TimeProvider.getLocalDate());

		File wordFile = getTestResourceFile("empty-word.docx");
		File excelFile = getTestResourceFile("empty-excel.xlsx");
		File powerpointFile = getTestResourceFile("empty-powerpoint.pptx");

		Content wordContent = contentManager.createMajor(users.adminIn(zeCollection), wordFile.getName(), contentManager.upload(wordFile));
		Content excelContent = contentManager.createMajor(users.adminIn(zeCollection), excelFile.getName(), contentManager.upload(excelFile));
		Content powerpointContent = contentManager.createMajor(users.adminIn(zeCollection), powerpointFile.getName(), contentManager.upload(powerpointFile));

		Document wordDocument = rm.newDocument().setTitle("Word document")
				.setFolder(folder).setContent(wordContent);
		Document excelDocument = rm.newDocument().setTitle("Excel document")
				.setFolder(folder).setContent(excelContent);
		Document powerpointDocument = rm.newDocument().setTitle("PowerPoint document")
				.setFolder(folder).setContent(powerpointContent);

		Facet mimeType = rm.newFacetField().setOrder(0).setFieldDataStoreCode("mimetype_s").setTitle("Type MIME");
		recordServices.execute(new Transaction(folder, wordDocument, excelDocument, powerpointDocument, mimeType));

		LogicalSearchQuery folder1999 = new LogicalSearchQuery(from(rm.documentSchemaType()).where(rm.document.folder()).isEqualTo("1999"));
		List<FacetVO> facets = searchPresenterService.getFacets(folder1999, facetStatus, Locale.FRENCH);

		assertThat(facets.get(0)).has(label("Type MIME"))
				.has(values(value("Microsoft Excel", "Microsoft Excel"),
						value("Microsoft PowerPoint", "Microsoft PowerPoint"),
						value("Microsoft Word", "Microsoft Word")));
	}

	@Test
	public void givenFieldAndQueryFacetsConfiguredWhenSearchingEverySchemaTypesThenReturnThoseWithValuesAndSchemas()
			throws Exception {
		recordServices.add(rm.newFacetQuery().setOrder(0).setTitle("Type")
				.withQuery("schema_s:folder*", "Dossiers")
				.withQuery("schema_s:document*", "Documents")
				.withQuery("schema_s:containerRecord*", "Contenants"));

		recordServices.add(rm.newFacetField().setOrder(1).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations"));
		recordServices.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("copyStatus_s").setTitle("Statut d'exemplaire"));
		recordServices.add(rm.newFacetField().setOrder(3).setFieldDataStoreCode("keywords_ss").setTitle("Mots-clés"));
		recordServices.add(rm.newFacetField().setOrder(4).setFieldDataStoreCode("type_s").setTitle("Schéma"));

		recordServices.add(rm.newFacetQuery().setOrder(5).setTitle("Création/Modification")
				.withQuery("modifiedOn_dt:[NOW-1MONTH TO NOW]", "Modifiés les 30 derniers jours")
				.withQuery("modifiedOn_dt:[NOW-7DAY TO NOW]", "Modifiés les 7 derniers jours")
				.withQuery("createdOn_dt:[NOW-1MONTH TO NOW]", "Créés les 30 derniers jours")
				.withQuery("createdOn_dt:[NOW-7DAY TO NOW]", "Créés les 7 derniers jours"));
		recordServices.add(rm.newFacetField().setOrder(6).setFieldDataStoreCode("administrativeUnitId_s")
				.setTitle("Unités administratives"));

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments, facetStatus, Locale.FRENCH);

		assertThat(facets.get(0)).has(label("Type")).has(values(
				value("schema_s:document*", "Documents"),
				value("schema_s:folder*", "Dossiers")));

		assertThat(facets.get(1)).has(label("Règles de conservations")).has(
				values(value(records.getRule2()), value(records.getRule1()), value(records.getRule4()),
						value(records.getRule3())));

		assertThat(facets.get(2)).has(label("Statut d'exemplaire")).has(
				values(value(CopyType.PRINCIPAL.getCode(), "Principal"), value(CopyType.SECONDARY.getCode(), "Secondaire")));

		assertThat(facets.get(4)).has(label("Unités administratives")).has(
				values(value(records.getUnit10a()), value(records.getUnit30c()), value(records.getUnit12b()),
						value(records.getUnit11b())));

	}

	@Test
	public void givenFacetsWithValuesInAlphabeticalOrdersThenInAlphabeticOrder()
			throws Exception {

		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.ALPHABETICAL));
		recordServices.add(rm.newFacetField().setOrder(1).setFieldDataStoreCode("copyStatus_s").setTitle("Statut d'exemplaire")
				.setOrderResult(FacetOrderType.ALPHABETICAL));
		recordServices.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("administrativeUnitId_s")
				.setTitle("Unités administratives").setOrderResult(FacetOrderType.ALPHABETICAL));

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments, facetStatus, Locale.FRENCH);

		assertThat(facets.get(0)).has(label("Règles de conservations")).has(
				values(value(records.getRule1()), value(records.getRule2()), value(records.getRule3()),
						value(records.getRule4())));

		assertThat(facets.get(1)).has(label("Statut d'exemplaire")).has(
				values(value(CopyType.PRINCIPAL.getCode(), "Principal"), value(CopyType.SECONDARY.getCode(), "Secondaire")));

		assertThat(facets.get(2)).has(label("Unités administratives")).has(
				values(value(records.getUnit10a()), value(records.getUnit11b()), value(records.getUnit12b()),
						value(records.getUnit30c())));

	}

	@Test
	public void givenSchemaFacetThenValuesAreObtainedFromSchemaLabels()
			throws Exception {

		Locale.setDefault(Locale.FRENCH);

		MetadataSchemaTypesBuilder typesBuilder = getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection);
		typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().addLabel(Language.French, "Ze folder");
		typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema().addLabel(Language.French, "Ze document");
		getModelLayerFactory().getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);

		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("schema_s").setTitle("Ze type"));

		List<FacetVO> facets = new SearchPresenterService(zeCollection, users.adminIn(zeCollection), getModelLayerFactory(), null).getFacets(allFoldersAndDocuments,
				facetStatus, Locale.FRENCH);

		assertThat(facets.get(0)).has(label("Ze type")).has(
				values(value(Document.DEFAULT_SCHEMA, "Ze document"), value(Folder.DEFAULT_SCHEMA, "Ze folder")));

	}

	@Test
	public void givenFacetOpenByDefaultAndNoUserOverrideThenFacetIsOpen()
			throws Exception {
		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(true));
		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments, facetStatus, Locale.FRENCH);
		assertThat(facets.get(0).isOpen()).isTrue();
	}

	@Test
	public void givenFacetNotOpenByDefaultAndNoUserOverrideThenFacetIsNotOpen()
			throws Exception {
		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(false));
		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments, facetStatus, Locale.FRENCH);
		assertThat(facets.get(0).isOpen()).isFalse();
	}

	@Test
	public void givenFacetOpenByDefaultAndUserOverrideThenFacetIsClosed()
			throws Exception {
		Facet facet;
		recordServices.add(facet = rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(true));
		facetStatus.put(facet.getId(), false);
		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments, facetStatus, Locale.FRENCH);
		assertThat(facets.get(0).isOpen()).isFalse();
	}

	//	@Test
	//	public void givenFacetIsVisisbleByDefaultThenOk() throws Exception {
	//		Facet facet;
	//		recordServices.add(facet = rm.newFacetField().setOrder(0).setFieldDataStoreCode("title_s")
	//				.setTitle("Titre").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(true));
	//
	//		assertThat(searchPresenterService.isFacetVisisble(facet)).isTrue();
	//	}
	//
	//	@Test
	//	public void givenFacetThenMakeItUnVisisbleByUserThenOk() throws Exception {
	//		Facet facet;
	//		recordServices.add(facet = rm.newFacetField().setOrder(0).setFieldDataStoreCode("title_s")
	//				.setTitle("Titre").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(true));
	//
	//		addRoleToFolderDefaultTitleMetadata("M");
	//
	//		assertThat(searchPresenterService.isFacetVisisble(facet)).isFalse();
	//	}
	//
	//
	//	@Test
	//	public void givenFacetItIsVisisbleByUsingAValidRoleForUserThenOk() throws Exception {
	//		Facet facet;
	//		recordServices.add(facet = rm.newFacetField().setOrder(0).setFieldDataStoreCode("title_s")
	//				.setTitle("Titre").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(true));
	//
	//		addRoleToFolderDefaultTitleMetadata("RGD");
	//
	//		assertThat(searchPresenterService.isFacetVisisble(facet)).isTrue();
	//	}
	//
	//	@Test
	//	public void givenFacetThenMakeItVisisbleByProvidingSchemaTypeThatIsNotRoleRestritedForTheMetadataThenOk() throws Exception {
	//		Facet facet;
	//		recordServices.add(facet = rm.newFacetField().setOrder(0).setFieldDataStoreCode("title_s")
	//				.setTitle("Titre").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(true));
	//
	//		addRoleToFolderDefaultTitleMetadata("M");
	//		searchPresenterService.setMetadataSchemaTypesList(asList(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(Document.SCHEMA_TYPE)));
	//		assertThat(searchPresenterService.isFacetVisisble(facet)).isTrue();
	//	}
	//
	//	@Test
	//	public void givenFacetThenMakeItNotVisisbleByProvidingSchemaTypeThatHaveARoleRestritionForTheMetadataThenOk() throws Exception {
	//		Facet facet;
	//		recordServices.add(facet = rm.newFacetField().setOrder(0).setFieldDataStoreCode("title_s")
	//				.setTitle("Titre").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(true));
	//
	//		addRoleToFolderDefaultTitleMetadata("M");
	//		searchPresenterService.setMetadataSchemaTypesList(asList(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(Folder.SCHEMA_TYPE)));
	//		assertThat(searchPresenterService.isFacetVisisble(facet)).isFalse();
	//	}

	private void addRoleToFolderDefaultTitleMetadata(String role)
			throws OptimisticLocking {
		final MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		final MetadataSchemaTypesBuilder types = schemasManager.modify(zeCollection);
		final MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder;
		final MetadataBuilder builder;

		builder = types.getSchema(Folder.DEFAULT_SCHEMA).get(Schemas.TITLE.getLocalCode());

		MetadataAccessRestriction metadataAccessRestriction = new MetadataAccessRestriction(asList(role), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>());

		metadataAccessRestrictionBuilder = MetadataAccessRestrictionBuilder.modify(metadataAccessRestriction);
		builder.setAccessRestrictionBuilder(metadataAccessRestrictionBuilder);

		schemasManager.saveUpdateSchemaTypes(types);
	}

	@Test
	public void givenFacetNotOpenByDefaultAndUserOverrideThenFacetIsOpen()
			throws Exception {
		Facet facet;
		recordServices.add(facet = rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.RELEVANCE).setOpenByDefault(false));
		facetStatus.put(facet.getId(), true);
		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments, facetStatus, Locale.FRENCH);
		assertThat(facets.get(0).isOpen()).isTrue();
	}

	// ----------------------------------------------------

	//	private Condition<? super FacetVO> dataStoreCode(final String expectedDataStoreCode) {
	//		return new Condition<FacetVO>() {
	//			@Override
	//			public boolean matches(FacetVO value) {
	//				assertThat(value.getDatastoreCode()).describedAs("datastoreCode").isEqualTo(expectedDataStoreCode);
	//				return true;
	//			}
	//		};
	//	}

	private ExpectedFacetValue value(RetentionRule retentionRule) {
		int count = (int) searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folder.retentionRule()).isEqualTo(retentionRule));
		return new ExpectedFacetValue(retentionRule.getId(), retentionRule.getTitle(), -1);
	}

	private ExpectedFacetValue value(AdministrativeUnit unit) {
		int count = (int) searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folder.administrativeUnit()).isEqualTo(unit));
		return new ExpectedFacetValue(unit.getId(), unit.getTitle(), -1);
	}

	private ExpectedFacetValue value(DocumentType type) {
		return new ExpectedFacetValue(type.getId(), type.getTitle(), -1);
	}

	private ExpectedFacetValue value(String expectedValue, String expectedLabel) {
		return new ExpectedFacetValue(expectedValue, expectedLabel, -1);
	}

	private ExpectedFacetValue value(String expectedValue, String expectedLabel, int count) {
		return new ExpectedFacetValue(expectedValue, expectedLabel, count);
	}

	private static class ExpectedFacetValue {

		String value;
		String label;
		int count;

		private ExpectedFacetValue(String value, String label, int count) {
			this.value = value;
			this.label = label;
			this.count = count;
		}
	}

	private Condition<? super FacetVO> values(final ExpectedFacetValue... expectedFacetValues) {
		return new Condition<FacetVO>() {
			@Override
			public boolean matches(FacetVO value) {
				List<String> facetValues = new ArrayList<>();
				List<String> expectedFacetValuesCodes = new ArrayList<>();
				for (FacetValueVO facetValueVO : value.getValues()) {
					facetValues.add(facetValueVO.getValue());
				}

				for (ExpectedFacetValue facetValueVO : expectedFacetValues) {
					expectedFacetValuesCodes.add(facetValueVO.value);
				}

				assertThat(facetValues).isEqualTo(expectedFacetValuesCodes);

				for (int i = 0; i < expectedFacetValues.length; i++) {
					FacetValueVO facetValue = value.getValues().get(i);
					ExpectedFacetValue expectedFacetValue = expectedFacetValues[i];
					assertThat(facetValue.getValue()).describedAs("facet value's value").isEqualTo(expectedFacetValue.value);
					assertThat(facetValue.getLabel().contains(" - ") ? facetValue.getLabel().split(" - ")[1] : facetValue.getLabel()).describedAs("facet value's label").isEqualTo(expectedFacetValue.label);
					if (expectedFacetValue.count == -1) {
						assertThat(facetValue.getCount()).describedAs("facet value's count").isGreaterThan(0);
					} else {
						assertThat(facetValue.getCount()).describedAs("facet value's count").isEqualTo(expectedFacetValue.count);
					}
				}

				return true;
			}
		};
	}

	private Condition<? super FacetVO> label(final String expectedLabel) {
		return new Condition<FacetVO>() {
			@Override
			public boolean matches(FacetVO value) {
				assertThat(value.getLabel()).describedAs("title").isEqualTo(expectedLabel);
				return true;
			}
		};
	}

	private void clearExistingFacets() {
		for (Record facetRecord : searchServices.search(new LogicalSearchQuery(from(rm.facetSchemaType()).returnAll()))) {
			recordServices.logicallyDelete(facetRecord, User.GOD);
			recordServices.physicallyDelete(facetRecord, User.GOD);
		}
	}
}
