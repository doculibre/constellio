package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SlowTest
public class RMMigrationTo8_2_AcceptanceTest extends ConstellioTest {
	@Before
	public void init() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_8_1_1.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	@Test
	public void whenMigrateThenCreatedCartsStillExist() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		List<Record> carts = searchServices
				.search(new LogicalSearchQuery(from(rm.cart.schemaType()).returnAll()));
		assertThatRecords(carts).extractingMetadatas(IDENTIFIER).containsOnly(tuple("00000000400"), tuple("00000000398"), tuple("00000000396"));
	}

	@Test
	public void whenMigrateThenRelationBetweenCartsAndRecordsIsReversed() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		assertThat(rm.getFolder("A01").getFavorites()).containsOnly("00000000400", "00000000396");
		assertThat(rm.getFolder("A10").getFavorites()).containsOnly("00000000398", "00000000396", "00000000400");
		assertThat(rm.getDocument("00000000125").getFavorites()).containsOnly("00000000396");
		assertThat(rm.getDocument("00000000101").getFavorites()).containsOnly("00000000398", "00000000400");
	}

	@Test(expected = MetadataSchemasRuntimeException.NoSuchMetadata.class)
	public void whenTryingToGetCartMetadataFoldersThenThrowException() {
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(
				Cart.DEFAULT_SCHEMA + "_" + "folders");
	}

	@Test(expected = MetadataSchemasRuntimeException.NoSuchMetadata.class)
	public void whenTryingToGetCartMetadataDocumentsThenThrowException() {
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(
				Cart.DEFAULT_SCHEMA + "_" + "documents");
	}

	@Test(expected = MetadataSchemasRuntimeException.NoSuchMetadata.class)
	public void whenTryingToGetCartMetadataContainersThenThrowException() {
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(
				Cart.DEFAULT_SCHEMA + "_" + "containers");
	}
}
