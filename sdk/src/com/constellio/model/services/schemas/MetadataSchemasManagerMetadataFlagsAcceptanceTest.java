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
package com.constellio.model.services.schemas;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssential;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.solr.SolrDataStoreTypesFactory;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class MetadataSchemasManagerMetadataFlagsAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager otherMetadataSchemasManager;

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();

	@Test
	public void givenMultivalueStringMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsEssential).withABooleanMetadata());

		assertThat(zeSchema.stringMetadata().isEssential()).isTrue();
		assertThat(zeSchema.booleanMetadata().isEssential()).isFalse();
	}

	@Before
	public void setUp()
			throws Exception {

		ConfigManager configManager = getDataLayerFactory().getConfigManager();
		DataStoreTypesFactory typesFactory = new SolrDataStoreTypesFactory();
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		BatchProcessesManager batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		otherMetadataSchemasManager = new MetadataSchemasManager(configManager, typesFactory, taxonomiesManager,
				collectionsListManager, batchProcessesManager, searchServices);

	}
}
