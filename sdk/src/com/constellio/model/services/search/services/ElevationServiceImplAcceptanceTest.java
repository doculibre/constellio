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
}
