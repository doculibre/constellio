package com.constellio.model.services.search.services;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.Elevations;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.SolrSafeConstellioAcceptanceTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SlowTest
public class ElevationServiceImplAcceptanceTest extends SolrSafeConstellioAcceptanceTest {
	ElevationService elevationService;
	private RecordServices recordServices;
	private SearchServices searchServices;

	private String query1 = "query1";
	Record query1RecordElevationInZeCollection;
	Record query2RecordElevationInZeCollection;
	Record query1RecordAnswerInZeCollection;
	Record query1RecordElevationInBusinessCollection;

	private String query2 = "query2";
	private MetadataSchema zeSchema;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection);
		givenCollection(businessCollection);
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		SchemasRecordsServices zSchemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		zeSchema = zSchemas.userSchema();

		SchemasRecordsServices bSchemas = new SchemasRecordsServices(businessCollection, getModelLayerFactory());
		MetadataSchema businessSchema = bSchemas.userSchema();

		Transaction transaction = new Transaction();
		transaction.addUpdate(query1RecordElevationInZeCollection = recordServices.newRecordWithSchema(zeSchema));
		transaction.addUpdate(query2RecordElevationInZeCollection = recordServices.newRecordWithSchema(zeSchema));
		transaction.addUpdate(query1RecordAnswerInZeCollection = recordServices.newRecordWithSchema(zeSchema).set(
				zSchemas.userUsername(), query1));
		recordServices.execute(transaction);

		transaction = new Transaction();
		transaction.addUpdate(
				query1RecordElevationInBusinessCollection = recordServices.newRecordWithSchema(businessSchema));
		recordServices.execute(transaction);

		elevationService = new ElevationServiceImpl(getDataLayerFactory().getRecordsVaultServer(), getModelLayerFactory());
		elevationService.elevate(query1RecordElevationInZeCollection, query1);
		elevationService.elevate(query2RecordElevationInZeCollection, query2);
		elevationService.elevate(query1RecordElevationInBusinessCollection, query1);
	}

	@Test
	public void whenGetZeCollectionElevationForQuery1ThenReturnQuery1RecordElevationInZeCollection()
			throws Exception {
		List<DocElevation> elevation = elevationService
				.getCollectionElevation(zeCollection, query1);
		assertThat(elevation.size()).isEqualTo(1);
		assertThat(elevation.get(0).getId()).isEqualTo(query1RecordElevationInZeCollection.getId());
	}

	@Test
	public void whenGetZeCollectionElevationThenReturnQuery1RecordElevationInZeCollectionAndQuery2RecordElevationInZeCollection()
			throws Exception {
		Elevations elevations = elevationService.getCollectionElevations(zeCollection);
		assertThat(elevations.getQueryElevation(query1).getDocElevations().size()).isEqualTo(1);
		assertThat(elevations.getQueryElevation(query1).getDocElevations().get(0).getId())
				.isEqualTo(query1RecordElevationInZeCollection.getId());
		assertThat(elevations.getQueryElevation(query2).getDocElevations().size()).isEqualTo(1);
		assertThat(elevations.getQueryElevation(query2).getDocElevations().get(0).getId())
				.isEqualTo(query2RecordElevationInZeCollection.getId());
	}

	@Test
	public void givenEmptyQueryWhenElevateRecordThenRecordElevatedForSearchAllQuery()
			throws Exception {
		Record zeRecord;
		recordServices.add(zeRecord = recordServices.newRecordWithSchema(zeSchema));
		elevationService.elevate(zeRecord, null);
		assertThat(elevationService.getCollectionElevation(zeCollection, "*:*"))
				.isEqualTo(elevationService.getCollectionElevation(zeCollection, null))
				.isEqualTo(elevationService.getCollectionElevation(zeCollection, "")).isEqualTo(
				elevationService.getCollectionElevation(zeCollection, " "));
		assertThat(elevationService.getCollectionElevation(zeCollection, "*:*").size()).isEqualTo(1);
		assertThat(elevationService.getCollectionElevation(zeCollection, "*:*").get(0).getId()).isEqualTo(zeRecord.getId());
	}

    //TODO:
	/* Currently, because the all the Constellio collections store in one solr collection, it is not possible
	 * to change one Constellio collection configurations for the Elevation feature without any side effect on the
	 * other Constellio collections.
	 */
    @Ignore
	@Test
	public void whenRemoveZeCollectionElevationForQuery1ThenQuery1RecordElevationRemovedOnlyForQuery1AndZeCollection()
			throws Exception {
		elevationService.removeCollectionElevation(zeCollection, query1);
		List<DocElevation> elevation = elevationService
				.getCollectionElevation(zeCollection, query1);
		assertThat(elevation.size()).isEqualTo(0);
		elevation = elevationService
				.getCollectionElevation(businessCollection, query1);
		assertThat(elevation.size()).isEqualTo(2);
		assertThat(elevation).extracting("id").contains(query1RecordElevationInBusinessCollection.getId());
		elevation = elevationService
				.getCollectionElevation(zeCollection, query2);
		assertThat(elevation.size()).isEqualTo(1);
		assertThat(elevation.get(0).getId()).isEqualTo(query2RecordElevationInZeCollection.getId());
	}


    //TODO
	/* Currently, because the all the Constellio collections store in one solr collection, it is not possible
	 * to change one Constellio collection configurations for the Elevation feature without any side effect on the
	 * other Constellio collections.
	 */
	@Ignore
    @Test
	public void whenRemoveZeCollectionElevationThenAllZeCollectionElevationsRemoved()
			throws Exception {
		elevationService.removeCollectionElevations(zeCollection);
		List<DocElevation> elevation = elevationService
				.getCollectionElevation(zeCollection, query1);
		assertThat(elevation.size()).isEqualTo(0);
		elevation = elevationService
				.getCollectionElevation(businessCollection, query1);
		assertThat(elevation.size()).isEqualTo(2);
		assertThat(elevation).extracting("id").contains(query1RecordElevationInBusinessCollection.getId());
		elevation = elevationService
				.getCollectionElevation(zeCollection, query2);
		assertThat(elevation.size()).isEqualTo(0);
	}

}
