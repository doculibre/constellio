package com.constellio.app.modules.es.connectors.smb;

import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommand;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommandFactory;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommandFactory.SmbTestCommandType;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.data.utils.Factory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import jcifs.smb.NtlmPasswordAuthentication;
import org.assertj.core.groups.Tuple;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@InDevelopmentTest
//TODO Activate test
public class ConnectorSmbMinimalRealTest extends ConstellioTest {
	// Minimal share
	// smb://ip/minimal_share/
	// smb://ip/minimal_share/file.txt
	// smb://ip/minimal_share/folder/
	// smb://ip/minimal_share/folder/another_file.txt

	private String baseUrl = SDKPasswords.testSmbServer();
	private String share = SDKPasswords.testSmbShare();

	private String fileName = SmbTestParams.FILE_NAME;
	private String fileContent = SmbTestParams.FILE_CONTENT;

	private String folderName = SmbTestParams.FOLDER_NAME;

	private String anotherFileName = SmbTestParams.ANOTHER_FILE_NAME;
	private String anotherFileContent = SmbTestParams.ANOTHER_FILE_CONTENT;

	private NtlmPasswordAuthentication auth;

	private ESSchemasRecordsServices es;
	private ConnectorManager connectorManager;
	private ConnectorSmbInstance connectorInstance;

	private ConnectorLogger logger;
	private TestConnectorEventObserver eventObserver;

	private List<String> seeds;
	private List<String> inclusions;
	private List<String> exclusions;

	private SmbTestCommandFactory commandFactory;

	@Before
	public void setup()
			throws IOException {
		prepareSystem(withZeCollection().withConstellioESModule()
				.withAllTestUsers());
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		connectorManager = es.getConnectorManager();

		logger = new ConsoleConnectorLogger();
		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, "zeTestEventObserver"));

		auth = new NtlmPasswordAuthentication(SDKPasswords.testSmbDomain(), SDKPasswords.testSmbUsername(),
				SDKPasswords.testSmbPassword());
		commandFactory = new SmbTestCommandFactory(auth);

		populateMinimalShare();

		seeds = Arrays.asList(SDKPasswords.testSmbServer() + share);
		inclusions = seeds;
		exclusions = new ArrayList<>();
	}

	@Test
	// Confirm @SlowTest
	public void givenMinimalShareWhenCrawlingThenGetAllDocumentsAndFolders()
			throws RecordServicesException {

		createConnector(seeds, inclusions, exclusions);

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntilNoMoreRecordsFoundWithinTimeoutForSchemaTypes(Duration.standardMinutes(1),
						ConnectorSmbDocument.SCHEMA_TYPE,
						ConnectorSmbFolder.SCHEMA_TYPE);

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

	@Test
	// Confirm @SlowTest
	public void givenMinimalShareWithNewDocumentWhenCrawlingThenCrawlNewDocument()
			throws RecordServicesException {
		String newFileName = "newFile.txt";
		String newFileContent = "new File Content";
		Tuple newFileTuple = tuple(baseUrl + share + newFileName, newFileContent, true);

		createConnector(seeds, inclusions, exclusions);

		SmbTestCommand createFileCommand = commandFactory
				.get(SmbTestCommandType.CREATE_FILE, baseUrl + share + newFileName, newFileContent);
		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new NTimesWhileCommandIsExecuted(7, 5, createFileCommand));

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(3);

		assertThat(indexedDocuments).extracting(getDocumentFields())
				.containsOnly(getFetchedFileTuple(), getFetchedAnotherFileTuple(), newFileTuple);

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(2);

		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple(), getFetchedFolderTuple());
	}

	@Test
	// Confirm @SlowTest
	public void givenMinimalShareWithNewEmptyFolderWhenCrawlingThenCrawlNewEmptyFolder()
			throws RecordServicesException {
		String newFolderName = "newFolder/";
		Tuple newFolderTuple = tuple(baseUrl + share + newFolderName, true);

		createConnector(seeds, inclusions, exclusions);

		SmbTestCommand createFolderCommand = commandFactory
				.get(SmbTestCommandType.CREATE_FOLDER, baseUrl + share + newFolderName, "");
		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new NTimesWhileCommandIsExecuted(8, 5, createFolderCommand));

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(2);

		assertThat(indexedDocuments).extracting(getDocumentFields())
				.containsOnly(getFetchedFileTuple(), getFetchedAnotherFileTuple());

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(3);

		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple(), getFetchedFolderTuple(), newFolderTuple);
	}

	@Test
	public void givenMinimalShareWithNewFolderWithContentWhenCrawlingThenDetectNewFolderAndContent()
			throws RecordServicesException {
		String newFolderName = "newFolderName/";
		String newFileName = "newFile.txt";
		String newFileContent = "new content";
		Tuple newFolderTuple = tuple(baseUrl + share + newFolderName, true);
		Tuple newFileTuple = tuple(baseUrl + share + newFolderName + newFileName, newFileContent, true);

		SmbTestCommand createFolderCommand = commandFactory
				.get(SmbTestCommandType.CREATE_FOLDER, baseUrl + share + newFolderName, "");
		SmbTestCommand createFileCommand = commandFactory
				.get(SmbTestCommandType.CREATE_FILE, baseUrl + share + newFolderName + newFileName, newFileContent);
		List<SmbTestCommand> commands = Arrays.<SmbTestCommand>asList(createFolderCommand, createFileCommand);

		createConnector(seeds, inclusions, exclusions);

		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new NTimesWhileCommandIsExecuted(8, 5, commands));

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(3);

		assertThat(indexedDocuments).extracting(getDocumentFields())
				.containsOnly(getFetchedFileTuple(), getFetchedAnotherFileTuple(), newFileTuple);

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(3);

		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple(), getFetchedFolderTuple(), newFolderTuple);
	}

	@Test
	// Confirm @SlowTest
	public void givenMinimalShareWithDeletedDocumentWhenCrawlingThenRemoveDocument()
			throws RecordServicesException {

		createConnector(seeds, inclusions, exclusions);

		SmbTestCommand deleteCommand = commandFactory.get(SmbTestCommandType.DELETE, baseUrl + share + fileName, "");
		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new NTimesWhileCommandIsExecuted(10, 5, deleteCommand));

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(1);

		assertThat(indexedDocuments).extracting(getDocumentFields())
				.containsOnly(getFetchedAnotherFileTuple());

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(2);

		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple(), getFetchedFolderTuple());
	}

	@Test
	// Confirm @SlowTest
	public void givenMinimalShareWithDeleteEmptyFolderWhenCrawlingThenRemoveFolder()
			throws RecordServicesException, IOException {
		String newFolder = "newFolder/";
		SmbTestCommand createFolderCommand = commandFactory
				.get(SmbTestCommandType.CREATE_FOLDER, baseUrl + share + newFolder, "");
		createFolderCommand.execute();

		createConnector(seeds, inclusions, exclusions);

		SmbTestCommand deleteCommand = commandFactory.get(SmbTestCommandType.DELETE, baseUrl + share + newFolder, "");
		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new NTimesWhileCommandIsExecuted(10, 5, deleteCommand));

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

	@Test
	// Confirm @SlowTest
	public void givenMinimalShareDeleteFolderWithContentWhenCrawlingThenRemoveFolderAndContent()
			throws RecordServicesException {

		createConnector(seeds, inclusions, exclusions);

		SmbTestCommand deleteCommand = commandFactory.get(SmbTestCommandType.DELETE, baseUrl + share + folderName, "");
		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new NTimesWhileCommandIsExecuted(10, 5, deleteCommand));

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

	@Test
	// Confirm @SlowTest
	public void givenMinimalShareWithModifiedDocumentWhenCrawlingThenUpdateDocument()
			throws RecordServicesException {
		String modifiedContent = "Modified Content";
		Tuple updatedFileTuple = tuple(baseUrl + share + fileName, modifiedContent, true);

		createConnector(seeds, inclusions, exclusions);

		SmbTestCommand updateCommand = commandFactory
				.get(SmbTestCommandType.UPDATE_FILE, baseUrl + share + fileName, modifiedContent);
		ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.crawlUntil(new NTimesWhileCommandIsExecuted(7, 5, updateCommand));

		// Documents
		List<ConnectorSmbDocument> indexedDocuments = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance));
		assertThat(indexedDocuments).hasSize(2);

		assertThat(indexedDocuments).extracting(getDocumentFields())
				.containsOnly(updatedFileTuple, getFetchedAnotherFileTuple());

		// Folders
		List<ConnectorSmbFolder> indexedFolders = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance));
		assertThat(indexedFolders).hasSize(2);

		assertThat(indexedFolders).extracting(getFolderFields())
				.containsOnly(getFetchedShareTuple(), getFetchedFolderTuple());
	}

	@After
	public void after() {
		eventObserver.close();
		SmbTestCommand cleanShare = commandFactory.get(SmbTestCommandType.CLEAN_SHARE, baseUrl + share, "");
		cleanShare.execute();
	}

	private void populateMinimalShare() {
		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, baseUrl + share, "");
		populateMinimalShare.execute();
	}

	private ConnectorSmbInstance createConnector(List<String> seeds, List<String> inclusions, List<String> exclusions)
			throws RecordServicesException {

		String connectorCode = "zeConnectorCode";
		String connectorTitle = "zeConnectorTitle";

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(connectorCode)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(connectorTitle));

		es.getRecordServices()
				.update(connectorInstance.getWrappedRecord());

		return connectorInstance;
	}

	private String[] getDocumentFields() {
		return new String[]{ConnectorSmbDocument.URL, ConnectorSmbDocument.PARSED_CONTENT, ConnectorDocument.FETCHED};
	}

	private String[] getFolderFields() {
		return new String[]{ConnectorSmbFolder.URL, ConnectorDocument.FETCHED};
	}

	private Tuple getFetchedFileTuple() {
		return tuple(baseUrl + share + fileName, fileContent, true);
	}

	private Tuple getFetchedAnotherFileTuple() {
		return tuple(baseUrl + share + folderName + anotherFileName, anotherFileContent, true);
	}

	private Tuple getFetchedShareTuple() {
		return tuple(baseUrl + share, true);
	}

	private Tuple getFetchedFolderTuple() {
		return tuple(baseUrl + share + folderName, true);
	}

	private class NTimesWhileCommandIsExecuted implements Factory<Boolean> {
		private int currentIteration = 0;
		private int maxIterations = 0;
		private int commandIteration = 0;
		private List<SmbTestCommand> commands;

		public NTimesWhileCommandIsExecuted(int maxIterations, int commandIteration, SmbTestCommand command) {
			this.maxIterations = maxIterations;
			this.commandIteration = commandIteration;
			this.commands = Arrays.asList(command);
		}

		public NTimesWhileCommandIsExecuted(int maxIterations, int commandIteration, List<SmbTestCommand> commands) {
			this.maxIterations = maxIterations;
			this.commandIteration = commandIteration;
			this.commands = commands;
		}

		@Override
		public Boolean get() {
			currentIteration++;
			if (currentIteration < maxIterations) {
				if (currentIteration == commandIteration) {
					for (SmbTestCommand command : commands) {
						command.execute();
					}
				}
				return false;
			} else {
				return true;
			}
		}
	}

}