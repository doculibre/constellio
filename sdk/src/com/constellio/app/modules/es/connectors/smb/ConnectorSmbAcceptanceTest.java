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
package com.constellio.app.modules.es.connectors.smb;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.mockito.Mockito;

import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.SlowTest;

public class ConnectorSmbAcceptanceTest extends ConstellioTest {
	private static final String FILE_NAME = "file.txt";
	private static final String FILE_CONTENT = "This file is not empty";
	private static final String FOLDER_NAME = "folder/";
	private static final String ANOTHER_FILE = "another_file.txt";
	private static final String ANOTHER_FILE_CONTENT = "Also not empty";
	private static final String NON_EXISTENT_FILE_NAME = "nonExistentFile.txt";
	private static final String NON_EXISTENT_SHARE = "smb://192.168.1.208/noshare/";

	private ConnectorManager connectorManager;
	private RecordServices recordServices;
	private ESSchemasRecordsServices es;

	private ConnectorSmbInstance connectorInstance;
	private ConnectorLogger logger = new ConsoleConnectorLogger();

	private String share;
	private String domain;
	private String username;
	private String password;

	private String file;
	private String folder;
	private String anotherFile;
	private String bigShare = "smb://192.168.1.208/sharebig/";

	private TestConnectorEventObserver eventObserver;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioESModule()
				.withAllTestUsers();

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		share = SDKPasswords.testSmbShare();
		domain = SDKPasswords.testSmbDomain();
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();

		// Structure
		// smb://ip/share/
		// smb://ip/share/file.txt
		// smb://ip/share/folder/
		// smb://ip/share/folder/another_file.txt

		file = share + FILE_NAME;
		folder = share + FOLDER_NAME;
		anotherFile = share + FOLDER_NAME + ANOTHER_FILE;

		eventObserver = newEventObserver();

	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	@InDevelopmentTest
	@SlowTest
	public void whenConnectorIsRunningThenFetchNewDocuments() {
		createConnector(share);

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new EndOfTraversalIsReached());

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(2);

		assertThat(indexedDocuments).extracting(getDocumentFields())
				.containsOnly(getFetchedFileTuple(), getFetchedAnotherFileTuple());

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(2);

		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple(), getFetchedFolderTuple());
	}

	private String[] getDocumentFields() {
		return Arrays.array(ConnectorSmbDocument.URL, ConnectorSmbDocument.PARSED_CONTENT, ConnectorDocument.FETCHED);
	}

	private String[] getFolderFields() {
		return Arrays.array(ConnectorSmbFolder.URL, ConnectorDocument.FETCHED);
	}

	private Tuple getFetchedFileTuple() {
		return tuple(file, FILE_CONTENT, true);
	}

	private Tuple getFetchedAnotherFileTuple() {
		return tuple(anotherFile, ANOTHER_FILE_CONTENT, true);
	}

	private Tuple getFetchedShareTuple() {
		return tuple(share, true);
	}

	private Tuple getFetchedFolderTuple() {
		return tuple(folder, true);
	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	@InDevelopmentTest
	@SlowTest
	public void whenConnectorIsRunningThenUpdateAlreadyFetchedDocuments()
			throws RecordServicesException {

		createConnector(share);

		createAndSaveReferenceDocument();

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new EndOfTraversalIsReached());

		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(2);

		String[] fields = Arrays.array(ConnectorSmbDocument.URL, ConnectorSmbDocument.PARSED_CONTENT, ConnectorDocument.FETCHED);

		assertThat(indexedDocuments).extracting(fields)
				.contains(getFetchedFileTuple());

		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(2);
	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	@InDevelopmentTest
	@SlowTest
	public void whenConnectorIsRunningThenRemoveDocumentsThatNoLongerExist()
			throws RecordServicesException {
		// Given 2 urls :
		// url where the filename does not exist. Share is available but the file is not there.
		// url that cannot be retrieved. Document could be deleted or the share could be unavailable.

		createConnector(share + NON_EXISTENT_FILE_NAME, NON_EXISTENT_SHARE);

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new GetJobsIsCalledNTimes(6));

		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));

		assertThat(indexedDocuments).isEmpty();
	}

	//	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	//	@InDevelopmentTest
	//	@SlowTest
	//	@LoadTest
	//	public void givenLotsOfContentWhenConnectorIsRunningThenFetchAllDocuments() {
	//		// Using pdfs from http://www.tldp.org/docs.html
	//		createConnector(bigShare);
	//
	//		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
	//				.crawlUntil(new GetJobsIsCalledNTimes(30));
	//
	//		List<ConnectorSmbDocument> indexedDocuments = es.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
	//		assertThat(indexedDocuments).hasSize(4);
	//
	//		String[] fields = Arrays.array(ConnectorSmbDocument.URL, ConnectorSmbDocument.PARSED_CONTENT, ConnectorDocument.FETCHED);
	//		Tuple shareTuple = tuple(share, null, true);
	//		Tuple fileTuple = tuple(file, FILE_CONTENT, true);
	//		Tuple folderTuple = tuple(folder, null, true);
	//		Tuple anotherFileTuple = tuple(anotherFile, ANOTHER_FILE_CONTENT, true);
	//
	//		assertThat(indexedDocuments).extracting(fields)
	//				.containsOnly(shareTuple, fileTuple, folderTuple, anotherFileTuple);
	//
	//		fail("To implement!");
	//	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	@InDevelopmentTest
	@SlowTest
	public void whenReachingEndOfTraversalThenAllDocumentsAreFetchedAndHaveTheSameTraversalCode()
			throws RecordServicesException {
		createConnector(share);

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new EndOfTraversalIsReached());

		ConnectorInstance latestConnectorInstance = connectorManager.getConnectorInstance(connectorInstance.getId());
		String traversalCode = latestConnectorInstance.getTraversalCode();

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(2);

		String[] fields = Arrays.array(ConnectorSmbDocument.URL, ConnectorSmbDocument.PARSED_CONTENT, ConnectorDocument.FETCHED,
				ConnectorDocument.TRAVERSAL_CODE);

		Tuple fileTuple = tuple(file, FILE_CONTENT, true, traversalCode);

		Tuple anotherFileTuple = tuple(anotherFile, ANOTHER_FILE_CONTENT, true, traversalCode);

		assertThat(indexedDocuments).extracting(fields)
				.containsOnly(fileTuple, anotherFileTuple);

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(2);

		fields = Arrays.array(ConnectorSmbFolder.URL, ConnectorDocument.FETCHED, ConnectorDocument.TRAVERSAL_CODE);
		Tuple shareTuple = tuple(share, true, traversalCode);
		Tuple folderTuple = tuple(folder, true, traversalCode);

		assertThat(indexedFolders).extracting(fields)
				.contains(shareTuple, folderTuple);
	}

	//	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	//	@InDevelopmentTest
	//	public void resumeTest() {
	//		fail("To implement!");
	//	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	@InDevelopmentTest
	@SlowTest
	public void whenStartingTraversalThenLogStartOfTraversal() {
		logger = Mockito.spy(logger);
		newEventObserver();

		createConnector(share);

		ConnectorCrawler.runningJobsSequentially(es, logger, eventObserver)
				.crawlUntil(new GetJobsIsCalledNTimes(1));

		verify(logger, times(1)).info(eq(ConnectorSmb.START_OF_TRAVERSAL), anyString(), anyMap());
	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	@InDevelopmentTest
	@SlowTest
	public void whenReachingEndOfTraversalThenLogEndOfTraversal() {
		logger = Mockito.spy(logger);
		newEventObserver();

		createConnector(share);

		ConnectorCrawler.runningJobsSequentially(es, logger, eventObserver)
				.crawlUntil(new GetJobsIsCalledNTimes(4));

		verify(logger, times(1)).info(eq(ConnectorSmb.END_OF_TRAVERSAL), anyString(), anyMap());
	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	public void givenIncludesWhenTraversingThenConsiderIncludes() {
		createConnector(share).setInclusions(asList(".*/$", ".*another.*"));

		flushRecord(connectorInstance.getWrappedRecord());

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new EndOfTraversalIsReached());

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).extracting("url")
				.containsOnly(anotherFile);

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(2);
		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple(), getFetchedFolderTuple());
	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	public void givenPathExcludePatternWhenTraversingThenConsiderExcludePattern() {
		createConnector(share).setExclusions(asList(folder));

		flushRecord(connectorInstance.getWrappedRecord());

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new EndOfTraversalIsReached());

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(1);

		assertThat(indexedDocuments).extracting(getDocumentFields())
				.containsOnly(getFetchedFileTuple());

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(1);

		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple());
	}

	//TODO Benoit - Se connecter à un serveur disponible depuis le web @Test
	public void givenRegexExcludePatternWhenTraversingThenConsiderExcludePattern() {
		createConnector(share).setExclusions(asList(".*another.*"));

		flushRecord(connectorInstance.getWrappedRecord());

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new EndOfTraversalIsReached());

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(1);

		assertThat(indexedDocuments).extracting(getDocumentFields())
				.containsOnly(getFetchedFileTuple());

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(2);

		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple(), getFetchedFolderTuple());
	}

	private TestConnectorEventObserver newEventObserver() {
		TestConnectorEventObserver eventObserver = new TestConnectorEventObserver(es,
				new DefaultConnectorEventObserver(es, logger,
						"testConnectorEventObserverResourceName"));
		return eventObserver;
	}

	private void createAndSaveReferenceDocument()
			throws RecordServicesException {
		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance)
				.setTitle(FILE_NAME)
				.setTraversalCode("zeAnotherTraversalCode")
				.setConnector(connectorInstance)
				.setFetched(true)
				.setUrl(file)
				.setParsedContent("zeContent");
		recordServices.add(document);
	}

	private ConnectorSmbInstance createConnector(String... seeds) {
		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("zeConnector")
				.setEnabled(true)
				.setSeeds(asList(seeds))
				.setUsername(username)
				.setPassword(password)
				.setDomain(domain))
				.setInclusions(new ArrayList<String>())
				.setExclusions(new ArrayList<String>());

		flushRecord(connectorInstance.getWrappedRecord());

		return connectorInstance;
	}

	private void flushRecord(Record record) {
		try {
			es.getRecordServices()
					.update(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private class GetJobsIsCalledNTimes implements Factory<Boolean> {
		private int getJobsLimit = 0;
		private int getJobsCounter = 0;

		public GetJobsIsCalledNTimes(int nTimes) {
			getJobsLimit = nTimes;
		}

		@Override
		public Boolean get() {
			boolean result = (getJobsCounter < getJobsLimit ? false : true);
			getJobsCounter++;
			return result;
		}
	}

	private class EndOfTraversalIsReached implements Factory<Boolean> {

		private int counter = 0;

		@Override
		public Boolean get() {
			List<ConnectorSmbDocument> indexedDocuments = es
					.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance)
							.andWhere(es.connectorDocument.fetched())
							.isFalse());

			List<ConnectorSmbFolder> indexedFolders = es
					.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance)
							.andWhere(es.connectorDocument.fetched())
							.isFalse());
			if (indexedDocuments.isEmpty() && indexedFolders.isEmpty()) {
				if (counter == 0) {
					counter++;
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
	}
}