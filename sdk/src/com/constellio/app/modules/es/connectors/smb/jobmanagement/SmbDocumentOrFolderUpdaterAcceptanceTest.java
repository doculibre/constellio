package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.model.connectors.DocumentSmbConnectorUrlCalculator;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SmbDocumentOrFolderUpdaterAcceptanceTest extends ConstellioTest {
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private SmbRecordService smbRecordService;
	private ConnectorSmbDocument document;
	private ConnectorSmbFolder folder;

	private String FILE_URL = SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE;
	private String FOLDER_URL = SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER;
	private long LAST_MODIFIED = System.currentTimeMillis();
	private String PARENT_URL = "parentUrl";
	// private LocalDateTime LAST_FETCHED = new LocalDateTime();

	String URL = "a";
	LocalDateTime LAST_MODIFIED2 = new LocalDateTime(LAST_MODIFIED);
	LocalDateTime LAST_FETCHED = new LocalDateTime();
	long SIZE = 11;
	String PERMISSIONS_HASH = "hash to replace";
	String TITLE = "title to replace";
	String PARSED_CONTENT = "content to replace";
	String LANGUAGE = "language to repplace";
	String EXTENSION = "extension to replace";
	List<String> TOKENS = Arrays.asList("t");
	String CONNECTOR = "connector id to replace";
	String TRAVERSAL_CODE = "traversal code to replace";

	@Before
	public void setup() {
		prepareSystem(withZeCollection().withConstellioESModule()
				.withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		connectorInstance = es.newConnectorSmbInstance()
				.setDomain(SmbTestParams.DOMAIN)
				.setUsername(SmbTestParams.USERNAME)
				.setPassword(SmbTestParams.PASSWORD)
				.setSeeds(asList(SmbTestParams.EXISTING_SHARE))
				.setCode(SmbTestParams.INSTANCE_CODE)
				.setTraversalCode(SmbTestParams.TRAVERSAL_CODE)
				.setInclusions(asList(SmbTestParams.EXISTING_SHARE))
				.setExclusions(asList(""))
				.setTitle(SmbTestParams.CONNECTOR_TITLE);
		es.getConnectorManager()
				.createConnector(connectorInstance);

		smbRecordService = new SmbRecordService(es, connectorInstance);
	}

	@Test
	public void givenFullDocumentDTOWhenUpdatingNewRecordThenUpdateEverything() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setLastModified(LAST_MODIFIED);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);
		smbFileDTO.setLength(SmbTestParams.EXISTING_FILE_LENGTH);
		smbFileDTO.setPermissionsHash(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(true);
		smbFileDTO.setIsDirectory(false);
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);
		smbFileDTO.setParsedContent(SmbTestParams.EXISTING_FILE_CONTENT);
		smbFileDTO.setLanguage(SmbTestParams.EXISTING_FILE_LANG);
		smbFileDTO.setExtension(SmbTestParams.EXISTING_FILE_EXT);
		smbFileDTO.setAllowTokens(SmbTestParams.ALLOW_TOKENS);
		smbFileDTO.setAllowShareTokens(SmbTestParams.ALLOW_SHARE_TOKENS);
		smbFileDTO.setDenyTokens(SmbTestParams.DENY_TOKENS);
		smbFileDTO.setDenyShareTokens(SmbTestParams.DENY_SHARE_TOKENS);

		document = es.newConnectorSmbDocument(connectorInstance);
		documentOrFolderUpdater.updateDocumentOrFolder(smbFileDTO, document, PARENT_URL, false);

		assertThat(document.getUrl()).isEqualTo(smbFileDTO.getUrl())
				.isNotEmpty();

		assertThat(document.getLastModified()).isEqualTo(new LocalDateTime(smbFileDTO.getLastModified()))
				.isNotNull();
		assertThat(document.getLastFetched()).isEqualTo(smbFileDTO.getLastFetchAttempt())
				.isNotNull();
		assertThat(document.getSize()).isEqualTo(smbFileDTO.getLength())
				.isNotNull();
		assertThat(document.getPermissionsHash()).isEqualTo(smbFileDTO.getPermissionsHash())
				.isNotNull();
		assertThat(document.getTitle()).isEqualTo(smbFileDTO.getName())
				.isNotNull();

		assertThat(document.getParsedContent()).isEqualTo(smbFileDTO.getParsedContent())
				.isNotNull();
		assertThat(document.getLanguage()).isEqualTo(smbFileDTO.getLanguage())
				.isNotNull();
		assertThat(document.getExtension()).isEqualTo(smbFileDTO.getExtension())
				.isNotNull();
		assertThat(document.getManualTokens()).isEqualTo(smbFileDTO.getAllowTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.SHARE_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getAllowShareTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.DENY_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getDenyTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.SHARE_DENY_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getDenyShareTokens())
				.isNotNull();

		assertThat(document.getLastFetchAttemptStatus()).isEqualTo(LastFetchedStatus.OK);
		assertThat(document.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(document.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(document.isFetched()).isTrue();
		assertThat(document.getLastFetchAttemptDetails()).isNull();

		assertThat(document.getErrorCode()).isNull();
		assertThat(document.getErrorMessage()).isNull();
		assertThat(document.getErrorsCount()).isZero();
		assertThat(document.getErrorStackTrace()).isNull();

		getModelLayerFactory().newRecordServices().recalculate(document);
		assertThat(document.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenFullDocumentDTOWhenUpdatingExistingRecordThenUpdateEverything() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setLastModified(LAST_MODIFIED);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);
		smbFileDTO.setLength(SmbTestParams.EXISTING_FILE_LENGTH);
		smbFileDTO.setPermissionsHash(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(true);
		smbFileDTO.setIsDirectory(false);
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);
		smbFileDTO.setParsedContent(SmbTestParams.EXISTING_FILE_CONTENT);
		smbFileDTO.setLanguage(SmbTestParams.EXISTING_FILE_LANG);
		smbFileDTO.setExtension(SmbTestParams.EXISTING_FILE_EXT);
		smbFileDTO.setAllowTokens(SmbTestParams.ALLOW_TOKENS);
		smbFileDTO.setAllowShareTokens(SmbTestParams.ALLOW_SHARE_TOKENS);
		smbFileDTO.setDenyTokens(SmbTestParams.DENY_TOKENS);
		smbFileDTO.setDenyShareTokens(SmbTestParams.DENY_SHARE_TOKENS);

		document = es.newConnectorSmbDocument(connectorInstance)
				.setUrl("a")
				.setLastModified(new LocalDateTime())
				.setLastFetched(new LocalDateTime())
				.setSize(11)
				.setPermissionsHash("hash to replace")
				.setTitle("title to replace")
				.setParsedContent("content to replace")
				.setLanguage("es")
				.setExtension("no ext");
		document.setManualTokens("t")
				.set(Schemas.SHARE_TOKENS.getLocalCode(), Arrays.asList("t"))
				.set(Schemas.DENY_TOKENS.getLocalCode(), Arrays.asList("t"))
				.set(Schemas.SHARE_DENY_TOKENS.getLocalCode(), Arrays.asList("t"));
		document.setLastFetchAttemptStatus(LastFetchedStatus.PARTIAL);
		document.setConnector("id to replace")
				.setTraversalCode("traversal code to replace")
				.setFetched(false)
				.setLastFetchAttemptDetails("");

		documentOrFolderUpdater.updateDocumentOrFolder(smbFileDTO, document, PARENT_URL, false);

		assertThat(document.getUrl()).isEqualTo(smbFileDTO.getUrl())
				.isNotEmpty();

		assertThat(document.getLastModified()).isEqualTo(new LocalDateTime(smbFileDTO.getLastModified()))
				.isNotNull();
		assertThat(document.getLastFetched()).isEqualTo(smbFileDTO.getLastFetchAttempt())
				.isNotNull();
		assertThat(document.getSize()).isEqualTo(smbFileDTO.getLength())
				.isNotNull();
		assertThat(document.getPermissionsHash()).isEqualTo(smbFileDTO.getPermissionsHash())
				.isNotNull();
		assertThat(document.getTitle()).isEqualTo(smbFileDTO.getName())
				.isNotNull();

		assertThat(document.getParsedContent()).isEqualTo(smbFileDTO.getParsedContent())
				.isNotNull();
		assertThat(document.getLanguage()).isEqualTo(smbFileDTO.getLanguage())
				.isNotNull();
		assertThat(document.getExtension()).isEqualTo(smbFileDTO.getExtension())
				.isNotNull();
		assertThat(document.getManualTokens()).isEqualTo(smbFileDTO.getAllowTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.SHARE_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getAllowShareTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.DENY_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getDenyTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.SHARE_DENY_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getDenyShareTokens())
				.isNotNull();

		assertThat(document.getLastFetchAttemptStatus()).isEqualTo(LastFetchedStatus.OK);
		assertThat(document.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(document.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(document.isFetched()).isTrue();
		assertThat(document.getLastFetchAttemptDetails()).isNull();

		assertThat(document.getErrorCode()).isNull();
		assertThat(document.getErrorMessage()).isNull();
		assertThat(document.getErrorsCount()).isZero();
		assertThat(document.getErrorStackTrace()).isNull();

		getModelLayerFactory().newRecordServices().recalculate(document);
		assertThat(document.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenFailedDocumentDTOWhenUpdatingExistingRecordThenUpdateErrors() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setLastModified(LAST_MODIFIED);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);
		smbFileDTO.setPermissionsHash(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(true);
		smbFileDTO.setIsDirectory(false);
		smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
		smbFileDTO.setParsedContent(SmbTestParams.EXISTING_FILE_CONTENT);
		smbFileDTO.setLanguage(SmbTestParams.EXISTING_FILE_LANG);
		smbFileDTO.setExtension(SmbTestParams.EXISTING_FILE_EXT);
		smbFileDTO.setAllowTokens(SmbTestParams.ALLOW_TOKENS);
		smbFileDTO.setAllowShareTokens(SmbTestParams.ALLOW_SHARE_TOKENS);
		smbFileDTO.setDenyTokens(SmbTestParams.DENY_TOKENS);
		smbFileDTO.setDenyShareTokens(SmbTestParams.DENY_SHARE_TOKENS);
		smbFileDTO.setErrorMessage("Error message");

		document = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(URL)
				.setLastModified(LAST_MODIFIED2)
				.setLastFetched(LAST_MODIFIED2)
				.setSize(SIZE)
				.setPermissionsHash(PERMISSIONS_HASH)
				.setTitle(TITLE)
				.setParsedContent(PARSED_CONTENT)
				.setLanguage(LANGUAGE)
				.setExtension(EXTENSION);
		document.setManualTokens(TOKENS)
				.set(Schemas.SHARE_TOKENS.getLocalCode(), TOKENS)
				.set(Schemas.DENY_TOKENS.getLocalCode(), TOKENS)
				.set(Schemas.SHARE_DENY_TOKENS.getLocalCode(), TOKENS);
		document.setLastFetchAttemptStatus(LastFetchedStatus.PARTIAL);
		document.setConnector(CONNECTOR)
				.setTraversalCode(TRAVERSAL_CODE)
				.setFetched(false)
				.setLastFetchAttemptDetails("");

		documentOrFolderUpdater.updateFailedDocumentOrFolder(smbFileDTO, document, PARENT_URL);

		// Unchanged

		assertThat(document.getLastModified()).isEqualTo(LAST_MODIFIED2);
		assertThat(document.getSize()).isEqualTo(SIZE);
		assertThat(document.getPermissionsHash()).isEqualTo(PERMISSIONS_HASH);
		assertThat(document.getTitle()).isEqualTo(TITLE);

		assertThat(document.getParsedContent()).isEqualTo(PARSED_CONTENT);
		assertThat(document.getLanguage()).isEqualTo(LANGUAGE);
		assertThat(document.getExtension()).isEqualTo(EXTENSION);
		assertThat(document.getManualTokens()).isEqualTo(TOKENS);
		assertThat(document.<List<String>>get(Schemas.SHARE_TOKENS.getLocalCode())).isEqualTo(TOKENS);
		assertThat(document.<List<String>>get(Schemas.DENY_TOKENS.getLocalCode())).isEqualTo(TOKENS);
		assertThat(document.<List<String>>get(Schemas.SHARE_DENY_TOKENS.getLocalCode())).isEqualTo(TOKENS);

		// Changed
		assertThat(document.getUrl()).isEqualTo(FILE_URL);
		assertThat(document.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(document.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(document.isFetched()).isTrue();
		assertThat(document.getLastFetchAttemptStatus()).isEqualTo(LastFetchedStatus.FAILED);
		assertThat(document.getLastFetched()).isEqualTo(LAST_FETCHED);
		assertThat(document.getLastFetchAttemptDetails()).isNotEmpty();

		assertThat(document.getErrorCode()).isNotEmpty();
		assertThat(document.getErrorMessage()).isNotEmpty();
		assertThat(document.getErrorsCount()).isNotZero();
		assertThat(document.getErrorStackTrace()).isNotEmpty();

		getModelLayerFactory().newRecordServices().recalculate(document);
		assertThat(document.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenFailedDocumentDTOWhenUpdatingNewRecordThenUpdateErrors() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setLastModified(LAST_MODIFIED);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);
		smbFileDTO.setPermissionsHash(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(true);
		smbFileDTO.setIsDirectory(false);
		smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
		smbFileDTO.setParsedContent(SmbTestParams.EXISTING_FILE_CONTENT);
		smbFileDTO.setLanguage(SmbTestParams.EXISTING_FILE_LANG);
		smbFileDTO.setExtension(SmbTestParams.EXISTING_FILE_EXT);
		smbFileDTO.setAllowTokens(SmbTestParams.ALLOW_TOKENS);
		smbFileDTO.setAllowShareTokens(SmbTestParams.ALLOW_SHARE_TOKENS);
		smbFileDTO.setDenyTokens(SmbTestParams.DENY_TOKENS);
		smbFileDTO.setDenyShareTokens(SmbTestParams.DENY_SHARE_TOKENS);
		smbFileDTO.setErrorMessage("Error message");

		document = es.newConnectorSmbDocument(connectorInstance);
		documentOrFolderUpdater.updateFailedDocumentOrFolder(smbFileDTO, document, PARENT_URL);

		// Initial state
		assertThat(document.getLastModified()).isNull();
		assertThat(document.getSize()).isEqualTo(-3);
		assertThat(document.getPermissionsHash()).isNull();
		assertThat(document.getTitle()).isNull();

		assertThat(document.getParsedContent()).isNull();
		assertThat(document.getLanguage()).isNull();
		assertThat(document.getExtension()).isNull();
		assertThat(document.getManualTokens()).isEmpty();
		List<String> shareTokens = document.get(Schemas.SHARE_TOKENS.getLocalCode());
		assertThat(shareTokens).isEmpty();
		List<String> denyTokens = document.get(Schemas.DENY_TOKENS.getLocalCode());
		assertThat(denyTokens).isEmpty();
		List<String> denyShareTokens = document.get(Schemas.SHARE_DENY_TOKENS.getLocalCode());
		assertThat(denyShareTokens).isEmpty();

		// Changed
		assertThat(document.getUrl()).isEqualTo(FILE_URL);
		assertThat(document.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(document.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(document.isFetched()).isTrue();
		assertThat(document.getLastFetchAttemptStatus()).isEqualTo(LastFetchedStatus.FAILED);
		assertThat(document.getLastFetched()).isEqualTo(LAST_FETCHED);
		assertThat(document.getLastFetchAttemptDetails()).isNotEmpty();

		assertThat(document.getErrorCode()).isNotEmpty();
		assertThat(document.getErrorMessage()).isNotEmpty();
		assertThat(document.getErrorsCount()).isNotZero();
		assertThat(document.getErrorStackTrace()).isNotEmpty();

		getModelLayerFactory().newRecordServices().recalculate(document);
		assertThat(document.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenFullFolderDTOWhenUpdatingNewRecordThenUpdateEverything() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FOLDER_URL);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);

		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(false);
		smbFileDTO.setIsDirectory(true);
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);

		folder = es.newConnectorSmbFolder(connectorInstance);
		documentOrFolderUpdater.updateDocumentOrFolder(smbFileDTO, folder, PARENT_URL, false);

		assertThat(folder.getUrl()).isEqualTo(smbFileDTO.getUrl())
				.isNotEmpty();

		assertThat(folder.getLastFetched()).isEqualTo(smbFileDTO.getLastFetchAttempt())
				.isNotNull();

		assertThat(folder.getTitle()).isEqualTo(smbFileDTO.getName())
				.isNotNull();

		assertThat(folder.getLastFetchedStatus()).isEqualTo(LastFetchedStatus.OK);
		assertThat(folder.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(folder.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(folder.isFetched()).isTrue();

		assertThat(folder.getErrorCode()).isNull();
		assertThat(folder.getErrorMessage()).isNull();
		assertThat(folder.getErrorsCount()).isZero();
		assertThat(folder.getErrorStackTrace()).isNull();

		getModelLayerFactory().newRecordServices().recalculate(folder);
		assertThat(folder.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenFullFolderDTOWhenUpdatingExistingRecordThenUpdateEverything() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FOLDER_URL);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);

		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(false);
		smbFileDTO.setIsDirectory(true);
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);

		folder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(URL)
				.setLastFetched(LAST_FETCHED)
				.setTitle(TITLE)
				.setLastFetchedStatus(LastFetchedStatus.FAILED)
				.setConnector(CONNECTOR)
				.setTraversalCode(TRAVERSAL_CODE)
				.setFetched(false);

		documentOrFolderUpdater.updateDocumentOrFolder(smbFileDTO, folder, PARENT_URL, false);

		assertThat(folder.getUrl()).isEqualTo(smbFileDTO.getUrl())
				.isNotEmpty();

		assertThat(folder.getLastFetched()).isEqualTo(smbFileDTO.getLastFetchAttempt())
				.isNotNull();

		assertThat(folder.getTitle()).isEqualTo(smbFileDTO.getName())
				.isNotNull();

		assertThat(folder.getLastFetchedStatus()).isEqualTo(LastFetchedStatus.OK);
		assertThat(folder.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(folder.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(folder.isFetched()).isTrue();

		assertThat(folder.getErrorCode()).isNull();
		assertThat(folder.getErrorMessage()).isNull();
		assertThat(folder.getErrorsCount()).isZero();
		assertThat(folder.getErrorStackTrace()).isNull();

		getModelLayerFactory().newRecordServices().recalculate(folder);
		assertThat(folder.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenFailedFolderDTOWhenUpdatingNewRecordThen() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FOLDER_URL);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);

		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(false);
		smbFileDTO.setIsDirectory(true);
		smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
		smbFileDTO.setErrorMessage("Failed to connect: 0.0.0.0<00>/192.168.1.207");

		folder = es.newConnectorSmbFolder(connectorInstance);
		documentOrFolderUpdater.updateFailedDocumentOrFolder(smbFileDTO, folder, PARENT_URL);

		assertThat(folder.getUrl()).isEqualTo(smbFileDTO.getUrl())
				.isNotEmpty();

		assertThat(folder.getLastFetched()).isEqualTo(smbFileDTO.getLastFetchAttempt())
				.isNotNull();

		assertThat(folder.getTitle()).isNull();

		assertThat(folder.getLastFetchedStatus()).isEqualTo(LastFetchedStatus.FAILED);
		assertThat(folder.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(folder.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(folder.isFetched()).isTrue();

		assertThat(folder.getErrorCode()).isEqualTo("ErrorCode");
		assertThat(folder.getErrorMessage()).isEqualTo("Failed to connect: 0.0.0.0<00>/192.168.1.207");
		assertThat(folder.getErrorsCount()).isNotZero();
		assertThat(folder.getErrorStackTrace()).isEqualTo("Failed to connect: 0.0.0.0<00>/192.168.1.207");

		getModelLayerFactory().newRecordServices().recalculate(folder);
		assertThat(folder.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenFailedFolderDTOWhenUpdatingExistingRecordThen() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FOLDER_URL);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);

		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(false);
		smbFileDTO.setIsDirectory(true);
		smbFileDTO.setStatus(SmbFileDTOStatus.FAILED_DTO);
		smbFileDTO.setErrorMessage("Failed to connect: 0.0.0.0<00>/192.168.1.207");

		folder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(URL)
				.setLastFetched(LAST_FETCHED)
				.setTitle(TITLE)
				.setLastFetchedStatus(LastFetchedStatus.FAILED)
				.setConnector(CONNECTOR)
				.setTraversalCode(TRAVERSAL_CODE)
				.setFetched(false);

		documentOrFolderUpdater.updateFailedDocumentOrFolder(smbFileDTO, folder, PARENT_URL);

		assertThat(folder.getUrl()).isEqualTo(smbFileDTO.getUrl())
				.isNotEmpty();

		assertThat(folder.getLastFetched()).isEqualTo(smbFileDTO.getLastFetchAttempt())
				.isNotNull();

		assertThat(folder.getTitle()).isEqualTo(TITLE);

		assertThat(folder.getLastFetchedStatus()).isEqualTo(LastFetchedStatus.FAILED);
		assertThat(folder.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(folder.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(folder.isFetched()).isTrue();

		assertThat(folder.getErrorCode()).isEqualTo("ErrorCode");
		assertThat(folder.getErrorMessage()).isEqualTo("Failed to connect: 0.0.0.0<00>/192.168.1.207");
		assertThat(folder.getErrorsCount()).isNotZero();
		assertThat(folder.getErrorStackTrace()).isEqualTo("Failed to connect: 0.0.0.0<00>/192.168.1.207");

		getModelLayerFactory().newRecordServices().recalculate(folder);
		assertThat(folder.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenUnmodifiedDocumentWhenUpdatingRecordThenMinimalUpdate() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		smbFileDTO.setUrl(FILE_URL);
		smbFileDTO.setLastModified(LAST_MODIFIED);
		smbFileDTO.setLastFetchAttempt(LAST_FETCHED);
		smbFileDTO.setLength(SmbTestParams.EXISTING_FILE_LENGTH);
		smbFileDTO.setPermissionsHash(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		smbFileDTO.setName(SmbTestParams.EXISTING_FILE);
		smbFileDTO.setIsFile(true);
		smbFileDTO.setIsDirectory(false);
		smbFileDTO.setStatus(SmbFileDTOStatus.FULL_DTO);
		smbFileDTO.setParsedContent(SmbTestParams.EXISTING_FILE_CONTENT);
		smbFileDTO.setLanguage(SmbTestParams.EXISTING_FILE_LANG);
		smbFileDTO.setExtension(SmbTestParams.EXISTING_FILE_EXT);
		smbFileDTO.setAllowTokens(SmbTestParams.ALLOW_TOKENS);
		smbFileDTO.setAllowShareTokens(SmbTestParams.ALLOW_SHARE_TOKENS);
		smbFileDTO.setDenyTokens(SmbTestParams.DENY_TOKENS);
		smbFileDTO.setDenyShareTokens(SmbTestParams.DENY_SHARE_TOKENS);

		document = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(FILE_URL)
				.setParentUrl(PARENT_URL)
				.setLastModified(LAST_MODIFIED2)
				.setLastFetched(LAST_FETCHED)
				.setSize(SmbTestParams.EXISTING_FILE_LENGTH)
				.setPermissionsHash(SmbTestParams.EXISTING_FILE_PERMISSION_HASH)
				.setTitle(SmbTestParams.EXISTING_FILE)
				.setParsedContent(SmbTestParams.EXISTING_FILE_CONTENT)
				.setLanguage(SmbTestParams.EXISTING_FILE_LANG)
				.setExtension(SmbTestParams.EXISTING_FILE_EXT);
		document.setManualTokens(SmbTestParams.ALLOW_TOKENS)
				.set(Schemas.SHARE_TOKENS.getLocalCode(), SmbTestParams.ALLOW_SHARE_TOKENS)
				.set(Schemas.DENY_TOKENS.getLocalCode(), SmbTestParams.DENY_TOKENS)
				.set(Schemas.SHARE_DENY_TOKENS.getLocalCode(), SmbTestParams.DENY_SHARE_TOKENS);
		document.setLastFetchAttemptStatus(LastFetchedStatus.PARTIAL);
		document.setConnector(connectorInstance.getId())
				.setTraversalCode(connectorInstance.getTraversalCode())
				.setFetched(false)
				.setLastFetchAttemptDetails("");

		assertThat(document.getUrl()).isEqualTo(smbFileDTO.getUrl())
				.isNotEmpty();

		assertThat(document.getLastModified()).isEqualTo(new LocalDateTime(smbFileDTO.getLastModified()))
				.isNotNull();
		assertThat(document.getLastFetched()).isEqualTo(smbFileDTO.getLastFetchAttempt())
				.isNotNull();
		assertThat(document.getSize()).isEqualTo(smbFileDTO.getLength())
				.isNotNull();
		assertThat(document.getPermissionsHash()).isEqualTo(smbFileDTO.getPermissionsHash())
				.isNotNull();
		assertThat(document.getTitle()).isEqualTo(smbFileDTO.getName())
				.isNotNull();

		assertThat(document.getParsedContent()).isEqualTo(smbFileDTO.getParsedContent())
				.isNotNull();
		assertThat(document.getLanguage()).isEqualTo(smbFileDTO.getLanguage())
				.isNotNull();
		assertThat(document.getExtension()).isEqualTo(smbFileDTO.getExtension())
				.isNotNull();
		assertThat(document.getManualTokens()).isEqualTo(smbFileDTO.getAllowTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.SHARE_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getAllowShareTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.DENY_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getDenyTokens())
				.isNotNull();
		assertThat(document.<List<String>>get(Schemas.SHARE_DENY_TOKENS.getLocalCode())).isEqualTo(smbFileDTO.getDenyShareTokens())
				.isNotNull();

		assertThat(document.getLastFetchAttemptStatus()).isEqualTo(LastFetchedStatus.PARTIAL);
		assertThat(document.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(document.getTraversalCode()).isEqualTo(connectorInstance.getTraversalCode());
		assertThat(document.isFetched()).isFalse();
		assertThat(document.getLastFetchAttemptDetails()).isNull();

		assertThat(document.getErrorCode()).isNull();
		assertThat(document.getErrorMessage()).isNull();
		assertThat(document.getErrorsCount()).isZero();
		assertThat(document.getErrorStackTrace()).isNull();

		getModelLayerFactory().newRecordServices().recalculate(document);
		assertThat(document.getParentConnectorUrl()).isEqualTo(buildConnectorUrl(connectorInstance.getId(), PARENT_URL));
	}

	@Test
	public void givenFullFolderDTOWithoutParentWhenUpdatingNewRecordThenFetchstatusPartial() {
		SmbDocumentOrFolderUpdater documentOrFolderUpdater = new SmbDocumentOrFolderUpdater(connectorInstance, smbRecordService);
		SmbFileDTO smbFileDTO = new SmbFileDTO();
		folder = es.newConnectorSmbFolder(connectorInstance);
		documentOrFolderUpdater.updateDocumentOrFolder(smbFileDTO, folder, null, false);

		assertThat(folder.getLastFetchedStatus()).isEqualTo(LastFetchedStatus.PARTIAL);
	}

	private String buildConnectorUrl(String connectorId, String url) {
		return DocumentSmbConnectorUrlCalculator.calculate(url, connectorId);
	}
}