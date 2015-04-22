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
package com.constellio.data.dao.services.transactionLog;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;
import com.constellio.sdk.tests.SolrSDKToolsServices;
import com.constellio.sdk.tests.SolrSDKToolsServices.VaultSnapshot;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.LoadTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class XMLSecondTransactionLogManagerAcceptTest extends ConstellioTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLSecondTransactionLogManagerAcceptTest.class);

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();

	private File logBaseFolder;

	XMLSecondTransactionLogManager log;

	RecordServices recordServices;

	private AtomicInteger index = new AtomicInteger(-1);
	private List<String> recordTextValues = new ArrayList<>();

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(SecondTransactionLogManager.class);
		logBaseFolder = newTempFolder();

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(DataLayerConfiguration configuration) {
				doReturn(true).when(configuration).isSecondTransactionLogEnabled();
				doReturn(logBaseFolder).when(configuration).getSecondTransactionLogBaseFolder();
				doReturn(Duration.standardSeconds(5)).when(configuration).getSecondTransactionLogMergeFrequency();
			}
		});

		givenCollection(zeCollection);
		defineSchemasManager().using(schemas.withAStringMetadata());

		recordServices = getModelLayerFactory().newRecordServices();
		log = (XMLSecondTransactionLogManager) getDataLayerFactory().getSecondTransactionLogManager();
	}

	@LoadTest
	@Test
	public void whenMultipleThreadsAreAdding5000RecordsThenAllRecordsAreLogged()
			throws Exception {

		runAdding(5000);

	}

	@Test
	public void whenMultipleThreadsAreAdding500RecordsThenAllRecordsAreLogged()
			throws Exception {

		runAdding(500);

	}

	private void runAdding(final int nbRecordsToAdd)
			throws Exception {

		for (int i = 1; i <= nbRecordsToAdd; i++) {
			recordTextValues.add("The Hobbit - Episode " + i + " of " + nbRecordsToAdd);
		}

		final ThreadList<Thread> threads = new ThreadList<>();
		for (int i = 0; i < 10; i++) {

			threads.add(new Thread() {
				@Override
				public void run() {
					int arrayIndex;

					while ((arrayIndex = index.incrementAndGet()) < nbRecordsToAdd) {
						System.out.println((arrayIndex + 1) + " / " + nbRecordsToAdd);
						Record record = new TestRecord(zeSchema);

						record.set(zeSchema.stringMetadata(), recordTextValues.get(arrayIndex));
						try {
							recordServices.add(record);
						} catch (RecordServicesException e) {
							throw new RuntimeException(e);
						}
					}
				}
			});
		}
		threads.startAll();
		threads.joinAll();

		int i = 0;
		while (log.getFlushedFolder().list().length != 0) {
			Thread.sleep(100);
			i++;
			if (i > 300) {
				fail("Never committed");
			}
		}
		Thread.sleep(100);
		if (log.getUnflushedFolder().list().length != 0) {
			throw new RuntimeException("Unflushed folder not empty");
		}
		if (log.getFlushedFolder().list().length != 0) {
			throw new RuntimeException("Flushed folder not empty");
		}
		if (log.getUnflushedFolder().list().length != 0) {
			throw new RuntimeException("Unflushed folder not empty");
		}

		List<String> stringMetadataLines = new ArrayList<>();
		List<String> transactionLogs = getDataLayerFactory().getContentsDao().getFolderContents("tlogs");

		for (String id : transactionLogs) {
			InputStream logStream = getDataLayerFactory().getContentsDao().getContentInputStream(id, SDK_STREAM);
			for (String line : IOUtils.readLines(logStream)) {
				stringMetadataLines.add(line);
			}
		}

		for (String value : recordTextValues) {
			assertThat(stringMetadataLines).contains(zeSchema.stringMetadata().getDataStoreCode() + "=" + value);
		}

		verify(log, atLeast(500)).prepare(anyString(), any(BigVaultServerTransaction.class));
		reset(log);

		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		SolrSDKToolsServices solrSDKTools = new SolrSDKToolsServices(recordDao);
		VaultSnapshot beforeRebuild = solrSDKTools.snapshot();

		alterSomeDocuments();

		log.destroyAndRebuildSolrCollection();

		VaultSnapshot afterRebuild = solrSDKTools.snapshot();
		solrSDKTools.ensureSameSnapshots("vault altered", beforeRebuild, afterRebuild);

		for (String text : recordTextValues) {
			assertThat(getRecordsByStringMetadata(text)).hasSize(1);
		}

		verify(log, never()).prepare(anyString(), any(BigVaultServerTransaction.class));
	}

	private List<String> getRecordsByStringMetadata(String value) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		return searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(from(zeSchema.instance()).where(zeSchema.stringMetadata()).isEqualTo(value)));
	}

	private void alterSomeDocuments()
			throws Exception {

		BigVaultRecordDao recordDao = (BigVaultRecordDao) getDataLayerFactory().newRecordDao();

		String idOf42 = getRecordsByStringMetadata(recordTextValues.get(42)).get(0);
		String idOf66 = getRecordsByStringMetadata(recordTextValues.get(66)).get(0);
		String idOf72 = getRecordsByStringMetadata(recordTextValues.get(72)).get(0);

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:" + idOf42);

		RecordDTO recordDTO = recordDao.get(idOf66);

		SolrInputDocument documentUpdate = new ConstellioSolrInputDocument();
		documentUpdate.addField("id", idOf66);
		documentUpdate.addField("_version_", recordDTO.getVersion());
		documentUpdate.addField("stringMetadata_s", LangUtils.newMapWithEntry("set", "Mouhahahahaha"));

		recordDao.getBigVaultServer().addAll(new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setUpdatedDocuments(asList(documentUpdate))
				.addDeletedQuery("id:" + idOf42));

	}
}
