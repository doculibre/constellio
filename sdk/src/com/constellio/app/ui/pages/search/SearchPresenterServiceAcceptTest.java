/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

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
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetOrderType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class SearchPresenterServiceAcceptTest extends ConstellioTest {

	LocalDateTime threeYearsAgo = new LocalDateTime().minusYears(3);

	LogicalSearchQuery allFolders;
	LogicalSearchQuery allFoldersAndDocuments;

	RMSchemasRecordsServices rm;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;
	SearchPresenterService searchPresenterService;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		searchPresenterService = new SearchPresenterService(zeCollection, getAppLayerFactory());

		allFolders = new LogicalSearchQuery(from(rm.folderSchemaType()).returnAll());
		allFoldersAndDocuments = new LogicalSearchQuery(from(asList(rm.folderSchemaType(), rm.documentSchemaType())).returnAll());

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

		List<FacetVO> facets = searchPresenterService.getFacets(allFolders);
		assertThat(facets.get(0)).has(label("Règles de conservations")).has(dataStoreCode("retentionRuleId_s")).has(
				values(value(records.getRule2()), value(records.getRule4()), value(records.getRule1()),
						value(records.getRule3())));

		assertThat(facets.get(1)).has(label("Statut d'exemplaire")).has(dataStoreCode("copyStatus_s")).has(
				values(value(CopyType.PRINCIPAL.getCode(), "Principal"), value(CopyType.SECONDARY.getCode(), "Secondaire")));

		assertThat(facets.get(2)).has(label("Unités administratives")).has(dataStoreCode("administrativeUnitId_s")).has(
				values(value(records.getUnit10a()), value(records.getUnit30c()), value(records.getUnit11b()),
						value(records.getUnit12b())));

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

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments);

		assertThat(facets.get(0)).has(label("Type")).has(values(
				value("schema_s:document*", "Documents"),
				value("schema_s:folder*", "Dossiers")));

		assertThat(facets.get(1)).has(label("Règles de conservations")).has(dataStoreCode("retentionRuleId_s")).has(
				values(value(records.getRule2()), value(records.getRule1()), value(records.getRule4()),
						value(records.getRule3())));

		assertThat(facets.get(2)).has(dataStoreCode("copyStatus_s")).has(label("Statut d'exemplaire")).has(
				values(value(CopyType.PRINCIPAL.getCode(), "Principal"), value(CopyType.SECONDARY.getCode(), "Secondaire")));

		assertThat(facets.get(3)).has(label("Unités administratives")).has(dataStoreCode("administrativeUnitId_s")).has(
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

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments);

		assertThat(facets.get(0)).has(label("Règles de conservations")).has(dataStoreCode("retentionRuleId_s")).has(
				values(value(records.getRule1()), value(records.getRule2()), value(records.getRule3()),
						value(records.getRule4())));

		assertThat(facets.get(1)).has(dataStoreCode("copyStatus_s")).has(label("Statut d'exemplaire")).has(
				values(value(CopyType.PRINCIPAL.getCode(), "Principal"), value(CopyType.SECONDARY.getCode(), "Secondaire")));

		assertThat(facets.get(2)).has(label("Unités administratives")).has(dataStoreCode("administrativeUnitId_s")).has(
				values(value(records.getUnit10a()), value(records.getUnit11b()), value(records.getUnit12b()),
						value(records.getUnit30c())));

	}

	@Test
	public void givenFacetsWithLimitAndRelevanceOrderWhenSearchingAQueryWithToMuchFacetValuesThenLimitValuesBasedOnOrder()
			throws Exception {

		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.RELEVANCE).setElementPerPage(2));

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments);

		assertThat(facets.get(0)).has(label("Règles de conservations")).has(dataStoreCode("retentionRuleId_s")).has(
				values(value(records.getRule2()), value(records.getRule1())));
	}

	@Test
	public void givenFacetsWithLimitAndAlphabeticalOrderWhenSearchingAQueryWithToMuchFacetValuesThenLimitValuesBasedOnOrder()
			throws Exception {

		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.ALPHABETICAL).setElementPerPage(2));

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments);

		assertThat(facets.get(0)).has(label("Règles de conservations")).has(dataStoreCode("retentionRuleId_s")).has(
				values(value(records.getRule1()), value(records.getRule2())));
	}

	@Test
	public void givenFacetsWithLimitAndPagesInRelevanceOrderWhenSearchingAQueryWithToMuchFacetValuesThenLimitValuesBasedOnOrder()
			throws Exception {

		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.RELEVANCE).setElementPerPage(1).setPages(2));

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments);

		assertThat(facets.get(0)).has(label("Règles de conservations")).has(dataStoreCode("retentionRuleId_s")).has(
				values(value(records.getRule2()), value(records.getRule1())));
	}

	@Test
	public void givenFacetsWithLimitAndPagesInAlphabeticalOrderWhenSearchingAQueryWithToMuchFacetValuesThenLimitValuesBasedOnOrder()
			throws Exception {

		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("retentionRuleId_s")
				.setTitle("Règles de conservations").setOrderResult(FacetOrderType.ALPHABETICAL).setElementPerPage(1)
				.setPages(2));

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments);

		assertThat(facets.get(0)).has(label("Règles de conservations")).has(dataStoreCode("retentionRuleId_s")).has(
				values(value(records.getRule1()), value(records.getRule2())));
	}

	@Test
	public void givenAPlethoraOfFacetsThenOnlyShowFirstSevenOnes()
			throws Exception {

		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("categoryId_s").setTitle("Catégorie"));
		recordServices.add(rm.newFacetField().setOrder(1).setFieldDataStoreCode("retentionRuleId_s").setTitle("Règles"));
		recordServices.add(rm.newFacetField().setOrder(2).setFieldDataStoreCode("copyStatus_s").setTitle("Statut d'exemplaire"));
		recordServices.add(rm.newFacetField().setOrder(3).setFieldDataStoreCode("schema_s").setTitle("Type"));
		recordServices.add(rm.newFacetField().setOrder(4).setFieldDataStoreCode("activeRetentionPeriodCode_s").setTitle("actif"));
		recordServices.add(rm.newFacetField().setOrder(5).setFieldDataStoreCode("semiactiveRetentionPeriodCode_s")
				.setTitle("semi-active"));
		recordServices.add(rm.newFacetField().setOrder(6).setFieldDataStoreCode("archivisticStatus_s").setTitle(
				"Statut archivistique"));
		recordServices.add(rm.newFacetField().setOrder(7).setFieldDataStoreCode("mediaType_s").setTitle("Media type"));

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments);

		assertThat(facets).extracting("label").isEqualTo(asList("Catégorie", "Règles", "Statut d'exemplaire", "Type",
				"actif", "semi-active", "Statut archivistique"));

	}

	@Test
	public void givenSchemaFacetThenValuesAreObtainedFromSchemaLabels()
			throws Exception {

		MetadataSchemaTypesBuilder typesBuilder = getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection);
		typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().setLabel("Ze folder");
		typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema().setLabel("Ze document");
		getModelLayerFactory().getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);

		recordServices.add(rm.newFacetField().setOrder(0).setFieldDataStoreCode("schema_s").setTitle("Ze type"));

		List<FacetVO> facets = searchPresenterService.getFacets(allFoldersAndDocuments);

		assertThat(facets.get(0)).has(dataStoreCode("schema_s")).has(label("Ze type")).has(
				values(value(Document.DEFAULT_SCHEMA, "Ze document"), value(Folder.DEFAULT_SCHEMA, "Ze folder")));

	}

	// ----------------------------------------------------

	private Condition<? super FacetVO> dataStoreCode(final String expectedDataStoreCode) {
		return new Condition<FacetVO>() {
			@Override
			public boolean matches(FacetVO value) {
				assertThat(value.getDatastoreCode()).describedAs("datastoreCode").isEqualTo(expectedDataStoreCode);
				return true;
			}
		};
	}

	private ExpectedFacetValue value(RetentionRule retentionRule) {
		int count = (int) searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folderRetentionRule()).isEqualTo(retentionRule));
		return new ExpectedFacetValue(retentionRule.getId(), retentionRule.getTitle(), -1);
	}

	private ExpectedFacetValue value(AdministrativeUnit unit) {
		int count = (int) searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folderAdministrativeUnit()).isEqualTo(unit));
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
					assertThat(facetValue.getLabel()).describedAs("facet value's label").isEqualTo(expectedFacetValue.label);
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
