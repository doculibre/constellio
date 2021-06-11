package com.constellio.app.modules.es.connectors.smb;

import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommand;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommandFactory;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommandFactory.SmbTestCommandType;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.ConnectorCrawler;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import jcifs.smb.NtlmPasswordAuthentication;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.es.sdk.TestConnectorEvent.ADD_EVENT;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.DELETE_EVENT;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.MODIFY_EVENT;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;

//TODO Activate test
@InternetTest
public class ConnectorSmbAcceptanceTest extends ConstellioTest {
	private ConnectorManager connectorManager;
	private RecordServices recordServices;
	private ESSchemasRecordsServices es;

	private ConnectorSmbInstance connectorInstance;
	private ConnectorLogger logger = new ConsoleConnectorLogger();

	private TestConnectorEventObserver eventObserver;

	private List<String> seeds;
	private List<String> inclusions;
	private List<String> exclusions;
	private SmbTestCommandFactory commandFactory;

	private LocalDateTime TIME1 = new LocalDateTime();
	private LocalDateTime ONE_MINUTE_AFTER_TIME1 = TIME1.plusMinutes(1);
	private LocalDateTime TWO_MINUTES_AFTER_TIME1 = TIME1.plusMinutes(2);
	private LocalDateTime THREE_MINUTES_AFTER_TIME1 = TIME1.plusMinutes(3);
	private LocalDateTime FOUR_MINUTES_AFTER_TIME1 = TIME1.plusMinutes(4);
	private LocalDateTime FIVE_MINUTES_AFTER_TIME1 = TIME1.plusMinutes(5);
	private LocalDateTime TWO_WEEKS_AFTER_TIME1 = TIME1.plusDays(14);
	private LocalDateTime FOUR_WEEKS_AFTER_TIME1 = TIME1.plusDays(28);
	private LocalDateTime FIVE_WEEKS_AFTER_TIME1 = TIME1.plusDays(5 * 7);
	private LocalDateTime SIX_WEEKS_AFTER_TIME1 = TIME1.plusDays(6 * 7);
	private LocalDateTime SEVEN_WEEKS_AFTER_TIME1 = TIME1.plusDays(7 * 7);
	private LocalDateTime EIGHT_WEEKS_AFTER_TIME1 = TIME1.plusDays(8 * 7);

	private String SHARE_URL = SDKPasswords.testSmbServer() + SDKPasswords.testSmbShare();
	private String FILE_URL = SHARE_URL + SmbTestParams.FILE_NAME;
	private String FOLDER_URL = SHARE_URL + SmbTestParams.FOLDER_NAME;
	private String ANOTHER_FILE_URL = FOLDER_URL + SmbTestParams.ANOTHER_FILE_NAME;

	private String FOLDER2_URL = SHARE_URL + "folder2/";
	private String FILE2_URL = FOLDER2_URL + "file2.txt";
	private String FILE3_URL = SHARE_URL + "file3.txt";
	private String FILE4_URL = SHARE_URL + "file4.txt";
	private String FILE5_URL = FOLDER2_URL + "file5.txt";

	@Before
	public void setUp()
			throws Exception {
		// givenCollection(zeCollection).withConstellioESModule().withAllTestUsers();
		prepareSystem(withZeCollection().withConstellioESModule()
				.withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		eventObserver = new TestConnectorEventObserver(es, new DefaultConnectorEventObserver(es, logger, "crawlerObserver"));
		connectorManager.setCrawler(ConnectorCrawler.runningJobsSequentially(es, eventObserver)
				.withoutSleeps());
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(SDKPasswords.testSmbDomain(),
				SDKPasswords.testSmbUsername(),
				SDKPasswords.testSmbPassword());
		commandFactory = new SmbTestCommandFactory(auth);
		givenTimeIs(TIME1);
	}

	//broken @Test
	public void onFirstStartIfInvalidShareThenErrorInUserLog() {
		String seedUrl = SDKPasswords.testSmbServer() + "invalidShare/";
		seeds = Arrays.asList(seedUrl);
		inclusions = seeds;
		exclusions = new ArrayList<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(SmbTestParams.CONNECTOR_TITLE));

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		System.out.println("Fetch phase 1");
		SmbResult result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		System.out.println("Fetch phase 2");
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(ADD_EVENT, seedUrl));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(seedUrl, true, ONE_MINUTE_AFTER_TIME1, true));

		ConnectorSmbFolder folder = result.getFolders()
				.get(0);

		assertThat(folder.getErrorMessage()).isEqualTo("The network name cannot be found.");
		assertThat(folder.getErrorCode()).isEqualTo("ErrorCode");
		assertThat(folder.getErrorStackTrace()).isEqualTo("The network name cannot be found.");
		assertThat(folder.getErrorsCount()).isEqualTo(1);

	}

	//broken @Test
	public void onFirstStartIfInvalidPasswordThenErrorInUserLog() {
		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, SHARE_URL, "");
		populateMinimalShare.execute();

		seeds = Arrays.asList(SHARE_URL);
		inclusions = seeds;
		exclusions = new ArrayList<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword("invalidPassword")
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(SmbTestParams.CONNECTOR_TITLE));

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		System.out.println("Fetch phase 1");
		SmbResult result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		System.out.println("Fetch phase 2");
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(ADD_EVENT, SHARE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, ONE_MINUTE_AFTER_TIME1, true));

		ConnectorSmbFolder folder = result.getFolders()
				.get(0);

		assertThat(folder.getErrorMessage()).isEqualTo("Logon failure: unknown user name or bad password.");
		assertThat(folder.getErrorCode()).isEqualTo("ErrorCode");
		assertThat(folder.getErrorStackTrace()).isEqualTo("Logon failure: unknown user name or bad password.");
		assertThat(folder.getErrorsCount()).isEqualTo(1);

	}

	//broken @Test
	public void onFirstStartIfInvalidIPThenErrorInUserLog() {
		String seedUrl = "smb://ip/share/";
		seeds = Arrays.asList(seedUrl);
		inclusions = seeds;
		exclusions = new ArrayList<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword("invalidPassword")
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(SmbTestParams.CONNECTOR_TITLE));

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		System.out.println("Fetch phase 1");
		SmbResult result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		System.out.println("Fetch phase 2");
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(ADD_EVENT, seedUrl));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(seedUrl, true, ONE_MINUTE_AFTER_TIME1, true));

		ConnectorSmbFolder folder = result.getFolders()
				.get(0);

		assertThat(folder.getErrorMessage()).isEqualTo("Failed to connect to server");
		assertThat(folder.getErrorCode()).isEqualTo("ErrorCode");
		assertThat(folder.getErrorStackTrace()).isEqualTo("Failed to connect to server");
		assertThat(folder.getErrorsCount()).isEqualTo(1);

	}

	//broken @Test
	public void givenMinimalShareWhenTraversingTwiceThenFetchAndRefetchDocumentsAndFolders() {
		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, SHARE_URL, "");
		populateMinimalShare.execute();

		seeds = Arrays.asList(SHARE_URL);
		inclusions = seeds;
		exclusions = new ArrayList<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(SmbTestParams.CONNECTOR_TITLE));

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		System.out.println("Fetch phase 1");
		SmbResult result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		System.out.println("Fetch phase 2");
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(ADD_EVENT, SHARE_URL), tuple(ADD_EVENT, FILE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, ONE_MINUTE_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, ONE_MINUTE_AFTER_TIME1, true));

		// *
		// * ----------------- End of traversal --------------
		// *
		System.out.println("End of traversal");
		givenTimeIs(TWO_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(ADD_EVENT, FOLDER_URL), tuple(ADD_EVENT, ANOTHER_FILE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Refetch phase 3 --------------
		// *
		System.out.println("Refetch phase 3");
		givenTimeIs(THREE_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Refetch phase 4 --------------
		// *
		System.out.println("Refetch phase 4");
		givenTimeIs(FOUR_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Refetch phase 5 --------------
		// *
		System.out.println("Refetch phase 5");
		givenTimeIs(FIVE_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(MODIFY_EVENT, SHARE_URL), tuple(MODIFY_EVENT, FILE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true),
						tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true),
						tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- End of second traversal --------------
		// *
		System.out.println("End of second traversal");
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(MODIFY_EVENT, FOLDER_URL), tuple(MODIFY_EVENT, ANOTHER_FILE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true),
						tuple(ANOTHER_FILE_URL, true, TWO_WEEKS_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true),
						tuple(FOLDER_URL, true, TWO_WEEKS_AFTER_TIME1, true));

		SmbTestCommand cleanShare = commandFactory.get(SmbTestCommandType.CLEAN_SHARE, SHARE_URL, "");
		cleanShare.execute();
	}

	//broken @Test
	public void givenMinimalShareIsModifiedWhenTraversingThenUpdateCorrectly() {
		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, SHARE_URL, "");
		populateMinimalShare.execute();

		seeds = Arrays.asList(SHARE_URL);
		inclusions = seeds;
		exclusions = new ArrayList<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(SmbTestParams.CONNECTOR_TITLE));

		SmbResult result = fullyFetchShare();
		result = fullyFetchShare();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, TIME1, true), tuple(ANOTHER_FILE_URL, true, TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, TIME1, true), tuple(FOLDER_URL, true, TIME1, true));

		SmbTestCommand deleteFolder = commandFactory.get(SmbTestCommandType.DELETE, FOLDER_URL, "");
		SmbTestCommand createFolder2 = commandFactory.get(SmbTestCommandType.CREATE_FOLDER, FOLDER2_URL, "");

		SmbTestCommand createFile2 = commandFactory.get(SmbTestCommandType.CREATE_FILE, FILE2_URL, "file2 content");

		SmbTestCommand modifyFile = commandFactory.get(SmbTestCommandType.UPDATE_FILE, FILE_URL, "new content for file");

		deleteFolder.execute();
		createFolder2.execute();
		createFile2.execute();
		modifyFile.execute();

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		System.out.println("Fetch phase 1");
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, TIME1, true), tuple(ANOTHER_FILE_URL, true, TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, TIME1, true), tuple(FOLDER_URL, true, TIME1, true));

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		System.out.println("Fetch phase 2");
		givenTimeIs(TWO_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(MODIFY_EVENT, SHARE_URL), tuple(MODIFY_EVENT, FILE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true), tuple(ANOTHER_FILE_URL, true, TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, TWO_MINUTES_AFTER_TIME1, true), tuple(FOLDER_URL, true, TIME1, true));

		// *
		// * ----------------- Fetch phase 3 --------------
		// *
		System.out.println("Fetch phase 3");
		givenTimeIs(THREE_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(ADD_EVENT, FOLDER2_URL), tuple(ADD_EVENT, FILE2_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true),
						tuple(FILE2_URL, true, THREE_MINUTES_AFTER_TIME1, true),
						tuple(ANOTHER_FILE_URL, true, TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, TWO_MINUTES_AFTER_TIME1, true),
						tuple(FOLDER2_URL, true, THREE_MINUTES_AFTER_TIME1, true),
						tuple(FOLDER_URL, true, TIME1, true));

		// *
		// * ----------------- End of traversal --------------
		// *
		System.out.println("End of traversal");
		givenTimeIs(FOUR_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(DELETE_EVENT, FOLDER_URL), tuple(DELETE_EVENT, ANOTHER_FILE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true),
						tuple(FILE2_URL, true, THREE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, TWO_MINUTES_AFTER_TIME1, true),
						tuple(FOLDER2_URL, true, THREE_MINUTES_AFTER_TIME1, true));

		SmbTestCommand cleanShare = commandFactory.get(SmbTestCommandType.CLEAN_SHARE, SHARE_URL, "");
		cleanShare.execute();
	}

	//broken @Test
	public void givenNewContentAndModifiedContentAndNonModifiedContentWhenTraversingThenFetchInOrderNewThenModifiedThenNonModifiedAndPreserveParentRelationshipForTaxonomy() {
		// First traversal
		//
		// smb://ip/share/
		// smb://ip/share/file.txt
		// smb://ip/share/file3.txt
		// smb://ip/share/folder/
		// smb://ip/share/folder/another_file.txt
		//
		// Changes after first traversal and before second traversal
		//
		// smb://ip/share/file4.txt <--- New file (parent share)
		// smb://ip/share/file3.txt <--- Modified file (parent share)
		// smb://ip/share/file.txt <--- Non modified file (parent share)
		//
		// smb://ip/share/folder2/ <--- New folder (parent share)
		// smb://ip/share/folder2/file5.txt <--- New file (parent folder2)

		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, SHARE_URL, "");
		populateMinimalShare.execute();

		SmbTestCommand createFile3 = commandFactory.get(SmbTestCommandType.CREATE_FILE, FILE3_URL, "file3 content");
		createFile3.execute();

		seeds = Arrays.asList(SHARE_URL);
		inclusions = seeds;
		exclusions = new ArrayList<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(SmbTestParams.CONNECTOR_TITLE));

		SmbResult result = fullyFetchShare();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE_URL, true, TIME1, true), tuple(ANOTHER_FILE_URL, true, TIME1, true),
						tuple(FILE3_URL, true, TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, TIME1, true), tuple(FOLDER_URL, true, TIME1, true));

		SmbTestCommand createFile4 = commandFactory.get(SmbTestCommandType.CREATE_FILE, FILE4_URL, "file4 content");
		createFile4.execute();
		SmbTestCommand modifyFile3 = commandFactory.get(SmbTestCommandType.UPDATE_FILE, FILE3_URL, "new file 3 content");
		modifyFile3.execute();
		SmbTestCommand createFolder2 = commandFactory.get(SmbTestCommandType.CREATE_FOLDER, FOLDER2_URL, "");
		createFolder2.execute();
		SmbTestCommand createFile5 = commandFactory.get(SmbTestCommandType.CREATE_FILE, FILE5_URL, "file5 content");
		createFile5.execute();

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		System.out.println("Fetch phase 1");
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE3_URL, true, TIME1, true), tuple(FILE_URL, true, TIME1, true),
						tuple(ANOTHER_FILE_URL, true, TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, TIME1, true), tuple(FOLDER_URL, true, TIME1, true));

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		System.out.println("Fetch phase 2");
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsSequence(tuple(ADD_EVENT, FILE4_URL), tuple(MODIFY_EVENT, FILE3_URL), tuple(MODIFY_EVENT, FILE_URL),
						tuple(MODIFY_EVENT, SHARE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE4_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(FILE3_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(FILE_URL, true, ONE_MINUTE_AFTER_TIME1, true), tuple(ANOTHER_FILE_URL, true, TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, ONE_MINUTE_AFTER_TIME1, true), tuple(FOLDER_URL, true, TIME1, true));

		// *
		// * ----------------- Fetch phase 3 --------------
		// *
		System.out.println("Fetch phase 3");
		givenTimeIs(TWO_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsSequence(tuple(ADD_EVENT, FILE5_URL), tuple(ADD_EVENT, FOLDER2_URL),
						tuple(MODIFY_EVENT, ANOTHER_FILE_URL),
						tuple(MODIFY_EVENT, FOLDER_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FILE4_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(FILE3_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(FILE_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true),
						tuple(FILE5_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(SHARE_URL, true, ONE_MINUTE_AFTER_TIME1, true),
						tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true),
						tuple(FOLDER2_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- End of traversal --------------
		// *
		System.out.println("End of traversal");
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();
		result = tickAndGetAllDocumentsAndFolders();
		result = tickAndGetAllDocumentsAndFolders();
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(getParentForDocument(result, FILE_URL)).isEqualTo(getId(result, SHARE_URL));
		assertThat(getParentForDocument(result, FILE3_URL)).isEqualTo(getId(result, SHARE_URL));
		assertThat(getParentForDocument(result, FILE4_URL)).isEqualTo(getId(result, SHARE_URL));
		assertThat(getParentForFolder(result, FOLDER_URL)).isEqualTo(getId(result, SHARE_URL));
		assertThat(getParentForFolder(result, FOLDER2_URL)).isEqualTo(getId(result, SHARE_URL));
		assertThat(getParentForDocument(result, FILE5_URL)).isEqualTo(getId(result, FOLDER2_URL));
		assertThat(getParentForDocument(result, ANOTHER_FILE_URL)).isEqualTo(getId(result, FOLDER_URL));

		SmbTestCommand cleanShare = commandFactory.get(SmbTestCommandType.CLEAN_SHARE, SHARE_URL, "");
		cleanShare.execute();

	}

	private String getParentForDocument(SmbResult result, String url) {
		for (ConnectorSmbDocument document : result.getDocuments()) {
			if (StringUtils.equals(document.getUrl(), url)) {
				Record parentRecord = recordServices.getRecordByMetadata(es.connectorSmbFolder.connectorUrl(), document.getParentConnectorUrl());
				return parentRecord != null ? parentRecord.getId() : null;
			}
		}
		return "Parent not found for document";
	}

	private String getParentForFolder(SmbResult result, String url) {
		for (ConnectorSmbFolder folder : result.getFolders()) {
			if (StringUtils.equals(folder.getUrl(), url)) {
				Record parentRecord = recordServices.getRecordByMetadata(es.connectorSmbFolder.connectorUrl(), folder.getParentConnectorUrl());
				return parentRecord != null ? parentRecord.getId() : null;
			}
		}
		return "Parent not found for folder";
	}

	private String getId(SmbResult result, String url) {
		for (ConnectorSmbFolder folder : result.getFolders()) {
			if (StringUtils.equals(folder.getUrl(), url)) {
				return folder.getId();
			}
		}
		return "Id not found";
	}

	//broken @Test
	@Ignore
	public void givenANewWantedTraversalOrderWhenTraversingThenUseNewTraversalOrder() {
		fail("To implement!");
	}

	//broken @Test
	public void givenTwoConnectorsThatRunAtTheSameTimeWhenRunningThenNoProblems()
			throws InterruptedException {

		String SHARE_URL_A = SDKPasswords.testSmbServer() + SDKPasswords.testSmbShareA();
		String FILE_URL_A = SHARE_URL_A + SmbTestParams.EXISTING_FILE;
		String FOLDER_URL_A = SHARE_URL_A + SmbTestParams.EXISTING_FOLDER;
		String ANOTHER_FILE_A = FOLDER_URL_A + SmbTestParams.ANOTHER_FILE_NAME;

		SmbTestCommand populateMinimalShareA = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, SHARE_URL_A, "");
		populateMinimalShareA.execute();

		String SHARE_URL_B = SDKPasswords.testSmbServer() + SDKPasswords.testSmbShareB();
		String FILE_URL_B = SHARE_URL_B + SmbTestParams.EXISTING_FILE;
		String FOLDER_URL_B = SHARE_URL_B + SmbTestParams.EXISTING_FOLDER;
		String ANOTHER_FILE_B = FOLDER_URL_B + SmbTestParams.ANOTHER_FILE_NAME;

		SmbTestCommand populateMinimalShareB = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, SHARE_URL_B, "");
		populateMinimalShareB.execute();

		List<String> seedsA = Arrays.asList(SHARE_URL_A);
		List<String> inclusionsA = seedsA;
		List<String> exclusionsA = new ArrayList<>();

		ConnectorSmbInstance connectorInstanceA = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("connectora")
				.setEnabled(true)
				.setSeeds(seedsA)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusionsA)
				.setExclusions(exclusionsA)
				.setTitle("ConnectorA"));

		List<String> seedsB = Arrays.asList(SHARE_URL_B);
		List<String> inclusionsB = seedsB;
		List<String> exclusionsB = new ArrayList<>();

		ConnectorSmbInstance connectorInstanceB = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("connectorb")
				.setEnabled(true)
				.setSeeds(seedsB)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusionsB)
				.setExclusions(exclusionsB)
				.setTitle("ConnectorB"));

		connectorManager.getCrawler()
				.crawlNTimes(10);

		List<ConnectorSmbDocument> documentsA = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstanceA));
		assertThat(documentsA).extracting("url")
				.containsOnly(FILE_URL_A, ANOTHER_FILE_A);

		List<ConnectorSmbFolder> foldersA = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstanceA));
		assertThat(foldersA).extracting("url")
				.containsOnly(SHARE_URL_A, FOLDER_URL_A);

		List<ConnectorSmbDocument> documentsB = es
				.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstanceB));
		assertThat(documentsB).extracting("url")
				.containsOnly(FILE_URL_B, ANOTHER_FILE_B);

		List<ConnectorSmbFolder> foldersB = es
				.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstanceB));
		assertThat(foldersB).extracting("url")
				.containsOnly(SHARE_URL_B, FOLDER_URL_B);

		SmbTestCommand cleanShareA = commandFactory.get(SmbTestCommandType.CLEAN_SHARE, SHARE_URL_A, "");
		cleanShareA.execute();

		SmbTestCommand cleanShareB = commandFactory.get(SmbTestCommandType.CLEAN_SHARE, SHARE_URL_B, "");
		cleanShareB.execute();
	}

	//broken @Test
	public void whenResumingThenResumeFromResumeUrlAndConfirmThereAreNoUndueDeletes()
			throws RecordServicesException {
		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, SHARE_URL, "");
		populateMinimalShare.execute();

		seeds = Arrays.asList(SHARE_URL);
		inclusions = seeds;
		exclusions = new ArrayList<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(SmbTestParams.CONNECTOR_TITLE)
				.setResumeUrl(FOLDER_URL));

		// *
		// * ----------------- Fetch phase 1 --------------
		// *
		System.out.println("Fetch phase 1");
		SmbResult result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetchAttempt", "searchable")
				.isEmpty();

		// *
		// * ----------------- Fetch phase 2 --------------
		// *
		System.out.println("Fetch phase 2");
		givenTimeIs(ONE_MINUTE_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.isEmpty();

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.isEmpty();

		// *
		// * ----------------- Fetch phase 3 --------------
		// *
		System.out.println("Fetch phase 3");
		givenTimeIs(TWO_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(ADD_EVENT, ANOTHER_FILE_URL), tuple(ADD_EVENT, FOLDER_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 4. End of traversal --------------
		// *

		System.out.println("Fetch phase 4");
		givenTimeIs(THREE_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 5 --------------
		// *
		System.out.println("Fetch phase 5");
		givenTimeIs(FOUR_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 6 --------------
		// *
		System.out.println("Fetch phase 6");
		givenTimeIs(FIVE_MINUTES_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(ADD_EVENT, SHARE_URL), tuple(ADD_EVENT, FILE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_MINUTES_AFTER_TIME1, true),
						tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_MINUTES_AFTER_TIME1, true),
						tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 7 --------------
		// *
		System.out.println("Fetch phase 7");
		givenTimeIs(TWO_WEEKS_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(MODIFY_EVENT, FOLDER_URL), tuple(MODIFY_EVENT, ANOTHER_FILE_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 8. End of traversal --------------
		// *
		System.out.println("Fetch phase 8");
		givenTimeIs(FOUR_WEEKS_AFTER_TIME1);
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 9. Stopping connector --------------
		// *
		System.out.println("Fetch phase 9");
		givenTimeIs(FIVE_WEEKS_AFTER_TIME1);
		recordServices.update(connectorInstance.setEnabled(false));
		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 10. Starting connector with resume url --------------
		// *
		System.out.println("Fetch phase 10");
		givenTimeIs(SIX_WEEKS_AFTER_TIME1);

		connectorInstance = connectorInstance.setResumeUrl(FOLDER_URL);
		connectorInstance = connectorInstance.setEnabled(true);
		assertThat(connectorInstance.getResumeUrl()).isEqualTo(FOLDER_URL);
		es.getRecordServices()
				.update(connectorInstance);
		assertThat(connectorInstance.getResumeUrl()).isEqualTo(FOLDER_URL);

		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 11 --------------
		// *
		System.out.println("Fetch phase 11");
		givenTimeIs(SIX_WEEKS_AFTER_TIME1);

		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, TWO_WEEKS_AFTER_TIME1, true),
						tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 12 --------------
		// *
		System.out.println("Fetch phase 12");
		givenTimeIs(SEVEN_WEEKS_AFTER_TIME1);

		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.containsOnly(tuple(MODIFY_EVENT, ANOTHER_FILE_URL), tuple(MODIFY_EVENT, FOLDER_URL));

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, SEVEN_WEEKS_AFTER_TIME1, true),
						tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, SEVEN_WEEKS_AFTER_TIME1, true),
						tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		// *
		// * ----------------- Fetch phase 13. End of traversal --------------
		// *
		System.out.println("Fetch phase 13");
		givenTimeIs(EIGHT_WEEKS_AFTER_TIME1);

		result = tickAndGetAllDocumentsAndFolders();

		assertThat(eventObserver.newEvents()).extracting("eventType", "url")
				.isEmpty();

		assertThat(result.getDocuments()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(ANOTHER_FILE_URL, true, SEVEN_WEEKS_AFTER_TIME1, true),
						tuple(FILE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

		assertThat(result.getFolders()).extracting("url", "fetched", "lastFetched", "searchable")
				.containsOnly(tuple(FOLDER_URL, true, SEVEN_WEEKS_AFTER_TIME1, true),
						tuple(SHARE_URL, true, FIVE_MINUTES_AFTER_TIME1, true));

	}

	//broken @Test
	public void whenUpdatingConnectorInstanceMetadataThenMetadataDoesNotVanish()
			throws RecordServicesException {
		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, SHARE_URL, "");
		populateMinimalShare.execute();

		seeds = Arrays.asList(SHARE_URL);
		inclusions = seeds;
		exclusions = new ArrayList<>();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setEnabled(true)
				.setSeeds(seeds)
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setInclusions(inclusions)
				.setExclusions(exclusions)
				.setTitle(SmbTestParams.CONNECTOR_TITLE)
				.setResumeUrl(FOLDER_URL));

		// *
		// * ----------------- Fetch phase 8. End of traversal --------------
		// *
		System.out.println("Fetch phase 8");
		givenTimeIs(FOUR_WEEKS_AFTER_TIME1);
		tickAndGetAllDocumentsAndFolders();

		// *
		// * ----------------- Fetch phase 9. Stopping connector --------------
		// *
		System.out.println("Fetch phase 9");
		givenTimeIs(FIVE_WEEKS_AFTER_TIME1);
		recordServices.update(connectorInstance.setEnabled(false));
		tickAndGetAllDocumentsAndFolders();

		// *
		// * ----------------- Fetch phase 10. Starting connector with resume url --------------
		// *
		System.out.println("Fetch phase 10");
		givenTimeIs(SIX_WEEKS_AFTER_TIME1);

		connectorInstance = connectorInstance.setResumeUrl(FOLDER_URL);
		connectorInstance = connectorInstance.setEnabled(true);
		assertThat(connectorInstance.getResumeUrl()).isEqualTo(FOLDER_URL);
		es.getRecordServices()
				.update(connectorInstance);
		assertThat(connectorInstance.getResumeUrl()).isEqualTo(FOLDER_URL);

	}

	private SmbResult fullyFetchShare() {
		SmbResult results = new SmbResult(new ArrayList<ConnectorSmbDocument>(), new ArrayList<ConnectorSmbFolder>());
		// Dispatch job does not create event yet
		results = tickAndGetAllDocumentsAndFolders();
		boolean newEvents = true;
		while (newEvents) {

			results = tickAndGetAllDocumentsAndFolders();
			newEvents = !eventObserver.newEvents()
					.isEmpty();
		}
		return results;
	}

	private SmbResult tickAndGetAllDocumentsAndFolders() {
		connectorManager.getCrawler()
				.crawlNTimes(1);

		return new SmbResult(connectorDocuments(), connectorFolders());
	}

	private List<ConnectorSmbDocument> connectorDocuments() {
		return es.searchConnectorSmbDocuments(where(IDENTIFIER).isNotNull());
	}

	private List<ConnectorSmbFolder> connectorFolders() {
		return es.searchConnectorSmbFolders(where(IDENTIFIER).isNotNull());
	}

	private class SmbResult {
		List<ConnectorSmbDocument> documents;
		List<ConnectorSmbFolder> folders;

		public SmbResult(List<ConnectorSmbDocument> documents, List<ConnectorSmbFolder> folders) {
			this.documents = documents;
			this.folders = folders;
		}

		public List<ConnectorSmbDocument> getDocuments() {
			return documents;
		}

		public List<ConnectorSmbFolder> getFolders() {
			return folders;
		}
	}
}