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
package com.constellio.sdk.tests;

import static org.mockito.Mockito.spy;

import java.util.Arrays;

import org.junit.Before;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystemUtils;
import com.constellio.data.io.concurrent.filesystem.AtomicLocalFileSystem;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;

public class SolrSafeConstellioAcceptanceTest extends ConstellioTest {
	protected SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	protected SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();

	protected LogicalSearchCondition condition;
	protected SearchServices searchServices;
	protected RecordServices recordServices;
	protected RecordDao recordDao;

	protected Transaction transaction;
	protected ConditionTemplateFactory factory;

	@Before
	public void setUp() {
		DataLayerFactory dataLayerFactory = getDataLayerFactory();
		AtomicFileSystem configFileSystem = dataLayerFactory.getSolrServerFactory().getConfigFileSystem();
		AtomicFileSystem defaultSolrConfiguraions = new AtomicLocalFileSystem(new FoldersLocator().getSolrHomeConfFolder(),
				dataLayerFactory.getIOServicesFactory().newHashingService());

		if (AtomicFileSystemUtils.sync(defaultSolrConfiguraions, configFileSystem, ".*/data.*", ".*/hsperfdata.*")) {
			for (BigVaultServer server : dataLayerFactory.getSolrServers().getServers()) {
				server.reload();
			}
		}

		givenCollection(zeCollection, Arrays.asList(Language.French.getCode(), Language.English.getCode()));
		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, recordServices);

		transaction = new Transaction();
		factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);
	}

	protected Record newRecordOfZeSchema() {
		return recordServices.newRecordWithSchema(zeSchema.instance());
	}

}
