package com.constellio.model.services.taxonomies;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

public class TaxonomiesSearchServicesTest extends ConstellioTest {

	@Mock Record zeTaxonomySchemaType1Record, aRecord;
	String firstPath = "/collections/a/b";
	String secondPath = "/collections/c/b";
	List<String> zeTaxonomySchemaType1RecordPaths = asList(firstPath, secondPath);
	String taxonomySchemaType1 = "taxonomySchemaType1";
	String taxonomySchemaType2 = "taxonomySchemaType2";

	@Mock MetadataSchemaType zeSchemaType;
	@Mock MetadataSchema zeSchema;
	@Mock MetadataSchemaType anotherSchemaTypeUsingTaxonomy;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock TaxonomiesManager taxonomiesManager;
	@Mock SearchServices searchServices;
	TaxonomiesSearchServices services;
	@Mock TaxonomiesSearchOptions taxonomiesSearchOptions;

	String zeNotTaxonomy = "zeNotCollection";
	String zeTaxonomy = "zeTaxonomy";
	String anotherTaxonomy = "anotherTaxonomy";
	@Mock List<Record> theSearchResults;
	@Mock User bob;

	@Before
	public void setUp()
			throws Exception {
		services = new TaxonomiesSearchServices(searchServices, taxonomiesManager, metadataSchemasManager);
		// options = new TaxonomiesSearchOptions();
		when(metadataSchemasManager.getSchemaTypes(zeCollection)).thenReturn(metadataSchemaTypes);

		when(zeTaxonomySchemaType1Record.getSchemaCode()).thenReturn("taxonomySchemaType1_default");
		when(zeTaxonomySchemaType1Record.getCollection()).thenReturn(zeCollection);
		when(zeSchemaType.getDefaultSchema()).thenReturn(zeSchema);
		when(zeSchema.getCollection()).thenReturn(zeCollection);
		Taxonomy taxo = Taxonomy
				.createPublic("taxo1", "taxo1", zeCollection, Arrays.asList(taxonomySchemaType1, taxonomySchemaType2));
		when(taxonomiesManager.getTaxonomyFor(zeCollection, "taxonomySchemaType1")).thenReturn(taxo);
		when(taxonomiesManager.getTaxonomyFor(zeCollection, "taxonomySchemaType2")).thenReturn(taxo);
		when(zeTaxonomySchemaType1Record.getList(Schemas.PATH)).thenReturn((List) zeTaxonomySchemaType1RecordPaths);
		when(taxonomiesManager.getTaxonomyFor(zeNotTaxonomy, "taxonomySchemaType1")).thenReturn(null);
	}

	@Test
	public void whenGetRootRecordThenDoLogicalSearchAndReturnResults()
			throws Exception {

		ArgumentCaptor<LogicalSearchQuery> query = ArgumentCaptor.forClass(LogicalSearchQuery.class);

		SPEQueryResponse zeResponse = responseWith(theSearchResults);

		when(searchServices.query(query.capture())).thenReturn(zeResponse);

		List<Record> returnedRecords = services.getRootConcept(zeCollection, zeTaxonomy, taxonomiesSearchOptions);
		LogicalSearchCondition condition = query.getValue().getCondition();

		assertThat(returnedRecords).isSameAs(theSearchResults);
		assertThat(condition).isEqualTo(
				fromAllSchemasIn(zeCollection).where(Schemas.PARENT_PATH).is("/zeTaxonomy"));

	}

	@Test
	public void whenCheckingIfHasNonTaxonomyRecordsInStructureThenUseCorrectSearchQuery()
			throws Exception {

		ArgumentCaptor<LogicalSearchQuery> query = ArgumentCaptor.forClass(LogicalSearchQuery.class);
		when(zeTaxonomySchemaType1Record.getId()).thenReturn("zeTaxonomySchemaType1Record");
		when(taxonomiesSearchOptions.getIncludeStatus()).thenReturn(StatusFilter.ALL);

		services.findNonTaxonomyRecordsInStructure(zeTaxonomySchemaType1Record, taxonomiesSearchOptions);

		verify(searchServices).hasResults(query.capture());

		assertThat(query.getValue().getCondition()).isEqualTo(
				fromAllSchemasIn(query.getValue().getCondition().getCollection())
						.where(Schemas.PATH_PARTS).isEqualTo("_LAST_zeTaxonomySchemaType1Record")
						.andWhere(Schemas.SCHEMA).isNot(LogicalSearchQueryOperators.any(Arrays.asList(
						LogicalSearchQueryOperators.startingWithText(taxonomySchemaType1),
						LogicalSearchQueryOperators.startingWithText(taxonomySchemaType2)))));

	}

	private SPEQueryResponse responseWith(List<Record> records) {
		long recordsSize = (long) records.size();
		SPEQueryResponse response = mock(SPEQueryResponse.class);
		when(response.getRecords()).thenReturn(records);
		when(response.getNumFound()).thenReturn(recordsSize);
		return response;
	}

}
