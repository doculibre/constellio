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

import static com.constellio.app.modules.es.sdk.ESTestUtils.assertThatEventsObservedBy;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.addEvent;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.deleteEvent;
import static com.constellio.app.modules.es.sdk.TestConnectorEvent.modifyEvent;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.constellio.app.modules.es.connectors.smb.SmbService.SmbStatus;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.sdk.TestConnectorEventObserver;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DefaultConnectorEventObserver;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;

public class SmbFetchJobAcceptanceTest extends ConstellioTest {
	private static final String VALID_SHARE = "smb://ip/";
	private static final String VALID_DIRECTORY = "validDirectoty/";
	private static final String A_FILENAME = "file1.txt";
	private static final String ANOTHER_FILENAME = "file2.txt";
	private static final String NONEXISTENT_FILENAME = "nonexistent.txt";
	private static final String TRAVERSAL_CODE = "zeTaversalCode";
	private static final long LAST_FETCHED = 0123L;

	private ESSchemasRecordsServices es;
	@Mock private Connector connector;
	private ConnectorSmbInstance connectorInstance;
	private TestConnectorEventObserver eventObserver;
	private ConnectorSmbDocument connectorSmbDocument;
	private ConnectorSmbFolder connectorSmbFolder;
	private SmbFileDTO smbFileDTO;
	@Mock private SmbService smbService;
	private List<ConnectorDocument<?>> documentsOrFolders;
	private ConnectorLogger logger;

	private SmbFetchJob fetchJob;

	private String domain = "domain";
	private String username = "username";
	private String password = "password";
	private String instanceCode = "smbInstanceCode";

	@Before
	public void setup()
			throws RecordServicesException {
		MockitoAnnotations.initMocks(this);

		givenCollection(zeCollection).withConstellioESModule();
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		logger = new ConsoleConnectorLogger();

		when(connector.getLogger()).thenReturn(logger);
		eventObserver = new TestConnectorEventObserver(es,
				new DefaultConnectorEventObserver(es, logger, "defaultConnectorEventObserverResourceName"));

		connectorInstance = es.newConnectorSmbInstance()
				.setDomain(domain)
				.setUsername(username)
				.setPassword(password)
				.setSeeds(asList(VALID_SHARE))
				.setCode(instanceCode)
				.setTraversalCode(TRAVERSAL_CODE)
				.setInclusions(asList(VALID_SHARE))
				.setExclusions(asList(""));
		es.getConnectorManager()
				.createConnector(connectorInstance);

		documentsOrFolders = new ArrayList<>();

	}

	@Test
	public void givenNoDocumentWhenFetchingThenFetchNothing() {
		fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, logger);
		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.isEmpty();
	}

	@Test
	public void givenADocumentWithValidUrlWhenFetchingThenFetchOneDocument()
			throws RecordServicesException {

		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + A_FILENAME, getModelLayerFactory());

		smbFileDTO = new SmbFileDTO().setName(A_FILENAME)
				.setStatus(SmbStatus.OK);
		when(smbService.getSmbFileDTO(anyString())).thenReturn(smbFileDTO);

		fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, smbService, logger);
		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(modifyEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + A_FILENAME)));
	}

	@Test
	public void givenADirectoryWithValidUrlWhenFetchingThenFetchDirectoryAndContent()
			throws RecordServicesException {

		createFolderSaveAndAddToElementsPassedToJob(VALID_SHARE + VALID_DIRECTORY, getModelLayerFactory());

		smbFileDTO = new SmbFileDTO().setName("")
				.setStatus(SmbStatus.OK)
				.setIsDirectory(true)
				.setUrl(VALID_SHARE + VALID_DIRECTORY);
		when(smbService.getSmbFileDTO(anyString())).thenReturn(smbFileDTO);

		List<SmbFileDTO> smbDocuments = new ArrayList<>();

		smbDocuments.add(new SmbFileDTO().setName(A_FILENAME)
				.setUrl(VALID_SHARE + VALID_DIRECTORY + A_FILENAME)
				.setStatus(SmbStatus.OK));
		smbDocuments.add(new SmbFileDTO().setName(ANOTHER_FILENAME)
				.setUrl(VALID_SHARE + VALID_DIRECTORY + ANOTHER_FILENAME)
				.setStatus(SmbStatus.OK));
		when(smbService.getChildrenIn(smbFileDTO)).thenReturn(smbDocuments);

		when(connector.getLogger()).thenReturn(logger);

		fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, smbService, logger);
		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(modifyEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + VALID_DIRECTORY)
						.setTraversalCode(TRAVERSAL_CODE)
						.setFetched(true)), addEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + VALID_DIRECTORY + A_FILENAME)
						.setTraversalCode(TRAVERSAL_CODE)
						.setFetched(false)), addEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + VALID_DIRECTORY + ANOTHER_FILENAME)
						.setTraversalCode(TRAVERSAL_CODE)
						.setFetched(false)));
	}

	@Test
	public void givenAnEmptyDirectoryWithValidUrlWhenFetchingThenFetchOnlyDirectory()
			throws RecordServicesException {

		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + VALID_DIRECTORY, getModelLayerFactory());

		smbFileDTO = new SmbFileDTO().setName("")
				.setStatus(SmbStatus.OK);
		when(smbService.getSmbFileDTO(anyString())).thenReturn(smbFileDTO);

		List<SmbFileDTO> smbDocuments = new ArrayList<>();
		when(smbService.getChildrenIn(smbFileDTO)).thenReturn(smbDocuments);

		fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, smbService, logger);
		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(modifyEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + VALID_DIRECTORY)
						.setTraversalCode(TRAVERSAL_CODE)
						.setFetched(true)));
	}

	@Test
	public void givenADirectoryWithFailedSmbFileDTOWhenFetchingThenFetchOnlyDirectory()
			throws RecordServicesException {
		createFolderSaveAndAddToElementsPassedToJob(VALID_SHARE + VALID_DIRECTORY, getModelLayerFactory());

		smbFileDTO = new SmbFileDTO().setName("")
				.setStatus(SmbStatus.OK)
				.setIsDirectory(true)
				.setUrl(VALID_SHARE + VALID_DIRECTORY);
		when(smbService.getSmbFileDTO(anyString())).thenReturn(smbFileDTO);

		List<SmbFileDTO> smbFileDTOs = new ArrayList<>();

		smbFileDTOs.add(new SmbFileDTO().setName(A_FILENAME)
				.setUrl(VALID_SHARE + VALID_DIRECTORY + A_FILENAME)
				.setIsFile(true)
				.setStatus(SmbStatus.OK));
		smbFileDTOs.add(new SmbFileDTO().setName(ANOTHER_FILENAME)
				.setUrl(VALID_SHARE + VALID_DIRECTORY + ANOTHER_FILENAME)
				.setStatus(SmbStatus.FAIL)
				.setIsFile(true));
		when(smbService.getChildrenIn(smbFileDTO)).thenReturn(smbFileDTOs);

		when(connector.getLogger()).thenReturn(logger);

		fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, smbService, logger);
		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(modifyEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + VALID_DIRECTORY)
						.setTraversalCode(TRAVERSAL_CODE)
						.setFetched(true)), addEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + VALID_DIRECTORY + A_FILENAME)
						.setTraversalCode(TRAVERSAL_CODE)
						.setFetched(false)));
	}

	@Test
	public void givenANonExistentDocumentWhenFetchingThenDeleteRecord()
			throws RecordServicesException {
		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + NONEXISTENT_FILENAME, getModelLayerFactory());

		smbFileDTO = new SmbFileDTO().setStatus(SmbStatus.GONE);
		when(smbService.getSmbFileDTO(anyString())).thenReturn(smbFileDTO);

		fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, smbService, logger);
		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(deleteEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + NONEXISTENT_FILENAME)));
	}

	@Test
	public void givenADocumentWhoseExistenceIsUnknownWhenFetchingThenOnlyUpdateTraversalInformation()
			throws RecordServicesException {
		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + NONEXISTENT_FILENAME, getModelLayerFactory());

		smbFileDTO = new SmbFileDTO().setStatus(SmbStatus.UNKNOWN);
		when(smbService.getSmbFileDTO(anyString())).thenReturn(smbFileDTO);

		fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, smbService, logger);
		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(modifyEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + NONEXISTENT_FILENAME)
						.setLastFetchAttemptStatus(LastFetchedStatus.FAILED)));
	}

	//TODO Benoit Failing on integration server - @Test
	public void whenConvertingDTOToDocumentThenConvertAllRequiredMetadatas()
			throws RecordServicesException {
		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + A_FILENAME, getModelLayerFactory());

		ConnectorSmbDocument spiedDocument = Mockito.spy(connectorSmbDocument);
		SmbFetchJob fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, logger);

		SmbFileDTO rawDTO = new SmbFileDTO().setName(A_FILENAME)
				.setStatus(SmbStatus.OK)
				.setIsFile(true)
				.setIsDirectory(false)
				.setLength(5L)
				.setLastModified(System.currentTimeMillis())
				.setLastFetchAttempt(System.currentTimeMillis())
				.setParsedContent("Some content")
				.setPermissionsHash("some hash")
				.setUrl(VALID_SHARE + A_FILENAME)
				.setLanguage("en")
				.setErrorMessage("");

		// Clearing missing metadatas since dto was not created with SmbService
		rawDTO.getMissingMetadatas()
				.clear();

		SmbFileDTO spiedDTO = Mockito.spy(rawDTO);

		fetchJob.updateDocumentOrFolder(spiedDocument, spiedDTO, LastFetchedStatus.OK);

		verify(spiedDTO, times(1)).getName();
		verify(spiedDTO, times(1)).getLength();
		verify(spiedDTO, times(1)).getLastModified();
		verify(spiedDTO, times(1)).getLastFetchAttempt();
		verify(spiedDTO, times(1)).getParsedContent();
		verify(spiedDTO, times(1)).getPermissionsHash();
		verify(spiedDTO, times(1)).getLanguage();
		verify(spiedDTO, times(1)).getExtension();
		verify(spiedDTO, times(1)).getMissingMetadatas();
		verify(spiedDTO, times(1)).getErrorMessage();
		verifyNoMoreInteractions(spiedDTO);

		assertThat(spiedDocument.getTitle()).isEqualTo(spiedDTO.getName());
		assertThat(spiedDocument.getParsedContent()).isEqualTo(spiedDTO.getParsedContent());
		assertThat(spiedDocument.getPermissionsHash()).isEqualTo(spiedDTO.getPermissionsHash());
		assertThat(spiedDocument.getSize()).isEqualTo(spiedDTO.getLength());
		assertThat(spiedDocument.getLastModified()).isEqualTo(new LocalDateTime(spiedDTO.getLastModified()));
		assertThat(spiedDocument.getLastFetched()).isEqualTo(new LocalDateTime(spiedDTO.getLastFetchAttempt()));
		assertThat(spiedDocument.getFetched()).isTrue();
		assertThat(spiedDocument.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(spiedDocument.getTraversalCode()).isNotEmpty();
		assertThat(spiedDocument.getLastFetchAttemptDetails()).isEmpty();
		assertThat(spiedDocument.getLastFetchAttemptStatus()).isEqualTo(LastFetchedStatus.OK);

		Method[] declaredMethods = rawDTO.getClass()
				.getDeclaredMethods();
		assertThat(declaredMethods.length - 1).isEqualTo(2 * 13);

	}

	//TODO Benoit Failing on integration server - @Test
	public void givenErrorsDifferentFromHashWhenConvertingDTOToDocumentThenConvertAllRequiredMetadatas()
			throws RecordServicesException {
		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + A_FILENAME, getModelLayerFactory());

		ConnectorSmbDocument spiedDocument = Mockito.spy(connectorSmbDocument);
		SmbFetchJob fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, logger);

		SmbFileDTO rawDTO = new SmbFileDTO().setName(A_FILENAME)
				.setStatus(SmbStatus.OK)
				.setIsFile(true)
				.setIsDirectory(false)
				.setLength(5L)
				.setLastModified(System.currentTimeMillis())
				.setLastFetchAttempt(System.currentTimeMillis())
				.setParsedContent("Some content")
				.setPermissionsHash("some hash")
				.setUrl(VALID_SHARE + A_FILENAME)
				.setLanguage("en")
				.setErrorMessage("");

		rawDTO.getMissingMetadatas()
				.remove(SmbFileDTO.PERMISSIONS_HASH);

		SmbFileDTO spiedDTO = Mockito.spy(rawDTO);

		fetchJob.updateDocumentOrFolder(spiedDocument, spiedDTO, LastFetchedStatus.PARTIAL);

		verify(spiedDTO, times(1)).getName();
		verify(spiedDTO, times(1)).getLength();
		verify(spiedDTO, times(1)).getLastModified();
		verify(spiedDTO, times(1)).getLastFetchAttempt();
		verify(spiedDTO, times(1)).getParsedContent();
		verify(spiedDTO, times(1)).getPermissionsHash();
		verify(spiedDTO, times(1)).getLanguage();
		verify(spiedDTO, times(1)).getExtension();
		verify(spiedDTO, times(2)).getMissingMetadatas();
		verify(spiedDTO, times(1)).getErrorMessage();
		verifyNoMoreInteractions(spiedDTO);

		assertThat(spiedDocument.getTitle()).isEqualTo(spiedDTO.getName());
		assertThat(spiedDocument.getParsedContent()).isEqualTo(spiedDTO.getParsedContent());
		assertThat(spiedDocument.getPermissionsHash()).isEqualTo(spiedDTO.getPermissionsHash());
		assertThat(spiedDocument.getSize()).isEqualTo(spiedDTO.getLength());
		assertThat(spiedDocument.getLastModified()).isEqualTo(new LocalDateTime(spiedDTO.getLastModified()));
		assertThat(spiedDocument.getLastFetched()).isEqualTo(new LocalDateTime(spiedDTO.getLastFetchAttempt()));
		assertThat(spiedDocument.getFetched()).isTrue();
		assertThat(spiedDocument.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(spiedDocument.getTraversalCode()).isNotEmpty();
		assertThat(spiedDocument.getLastFetchAttemptDetails()).isNotEmpty();
		assertThat(spiedDocument.getLastFetchAttemptStatus()).isEqualTo(LastFetchedStatus.PARTIAL);

		Method[] declaredMethods = rawDTO.getClass()
				.getDeclaredMethods();
		assertThat(declaredMethods.length - 1).isEqualTo(2 * 13);

	}

	//TODO Benoit Failing on integration server - @Test
	public void givenErrorsWhenConvertingDTOToDocumentThenConvertAllRequiredMetadatas()
			throws RecordServicesException {
		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + A_FILENAME, getModelLayerFactory());

		ConnectorSmbDocument spiedDocument = Mockito.spy(connectorSmbDocument);
		SmbFetchJob fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, logger);

		SmbFileDTO rawDTO = new SmbFileDTO().setName(A_FILENAME)
				.setStatus(SmbStatus.OK)
				.setIsFile(true)
				.setIsDirectory(false)
				.setLength(5L)
				.setLastModified(System.currentTimeMillis())
				.setLastFetchAttempt(System.currentTimeMillis())
				.setParsedContent("Some content")
				.setPermissionsHash("some hash")
				.setUrl(VALID_SHARE + A_FILENAME)
				.setLanguage("en")
				.setErrorMessage("");

		SmbFileDTO spiedDTO = Mockito.spy(rawDTO);

		fetchJob.updateDocumentOrFolder(spiedDocument, spiedDTO, LastFetchedStatus.FAILED);

		verify(spiedDTO, times(1)).getName();
		verify(spiedDTO, times(1)).getLength();
		verify(spiedDTO, times(1)).getLastModified();
		verify(spiedDTO, times(1)).getLastFetchAttempt();
		verify(spiedDTO, times(1)).getParsedContent();
		verify(spiedDTO, times(1)).getPermissionsHash();
		verify(spiedDTO, times(1)).getLanguage();
		verify(spiedDTO, times(1)).getExtension();
		verify(spiedDTO, times(2)).getMissingMetadatas();
		verify(spiedDTO, times(1)).getErrorMessage();
		verifyNoMoreInteractions(spiedDTO);

		assertThat(spiedDocument.getTitle()).isEqualTo(spiedDTO.getName());
		assertThat(spiedDocument.getParsedContent()).isEqualTo(spiedDTO.getParsedContent());
		assertThat(spiedDocument.getPermissionsHash()).isEqualTo(spiedDTO.getPermissionsHash());
		assertThat(spiedDocument.getSize()).isEqualTo(spiedDTO.getLength());
		assertThat(spiedDocument.getLastModified()).isEqualTo(new LocalDateTime(spiedDTO.getLastModified()));
		assertThat(spiedDocument.getLastFetched()).isEqualTo(new LocalDateTime(spiedDTO.getLastFetchAttempt()));
		assertThat(spiedDocument.getFetched()).isTrue();
		assertThat(spiedDocument.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(spiedDocument.getTraversalCode()).isNotEmpty();
		assertThat(spiedDocument.getLastFetchAttemptDetails()).isNotEmpty();
		assertThat(spiedDocument.getLastFetchAttemptStatus()).isEqualTo(LastFetchedStatus.FAILED);

		Method[] declaredMethods = rawDTO.getClass()
				.getDeclaredMethods();
		assertThat(declaredMethods.length - 1).isEqualTo(2 * 13);

	}

	//TODO Benoit Failing on integration server - @Test
	public void whenConvertingDTOToFolderThenConvertAllRequiredMetadatas()
			throws RecordServicesException {
		createFolderSaveAndAddToElementsPassedToJob(VALID_SHARE, getModelLayerFactory());

		ConnectorSmbFolder spiedFolder = Mockito.spy(connectorSmbFolder);
		SmbFetchJob fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, logger);

		SmbFileDTO rawDTO = new SmbFileDTO().setName(VALID_SHARE)
				.setStatus(SmbStatus.OK)
				.setIsFile(false)
				.setIsDirectory(true)
				.setUrl(VALID_SHARE)
				.setLastFetchAttempt(LAST_FETCHED);

		SmbFileDTO spiedDTO = Mockito.spy(rawDTO);

		fetchJob.updateDocumentOrFolder(spiedFolder, spiedDTO, LastFetchedStatus.OK);

		verify(spiedDTO, times(1)).getName();
		verify(spiedDTO, times(1)).getLastFetchAttempt();
		verifyNoMoreInteractions(spiedDTO);

		assertThat(spiedFolder.getTitle()).isEqualTo(spiedDTO.getName());
		assertThat(spiedFolder.getFetched()).isTrue();
		assertThat(spiedFolder.getConnector()).isEqualTo(connectorInstance.getId());
		assertThat(spiedFolder.getTraversalCode()).isNotEmpty();

		Method[] declaredMethods = rawDTO.getClass()
				.getDeclaredMethods();
		assertThat(declaredMethods.length - 1).isEqualTo(2 * 13);

	}

	@Test
	@InDevelopmentTest
	public void givenADocumentThatDoesNotOriginateFromSeedsWhenTraversingThenDeleteDocument()
			throws RecordServicesException {
		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + NONEXISTENT_FILENAME, getModelLayerFactory());

		ConnectorSmbUtils smbUtils = Mockito.mock(ConnectorSmbUtils.class);
		when(smbUtils.isAccepted(anyString(), any(ConnectorSmbInstance.class))).thenReturn(false);
		fetchJob = Mockito
				.spy(new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, smbService, logger));
		doReturn(smbUtils).when(fetchJob)
				.getSmbUtils();

		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(deleteEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + NONEXISTENT_FILENAME)));
	}

	@Test
	@InDevelopmentTest
	public void givenAFailedSmbDTOWhenFetchingThenUpdatePartialContentAndIndicateLastFetchStatus()
			throws RecordServicesException {
		createDocumentSaveAndAddToElementsPassedToJob(VALID_SHARE + A_FILENAME, getModelLayerFactory());

		smbFileDTO = new SmbFileDTO().setName(A_FILENAME)
				.setStatus(SmbStatus.FAIL);
		when(smbService.getSmbFileDTO(anyString())).thenReturn(smbFileDTO);

		fetchJob = new SmbFetchJob(connector, documentsOrFolders, connectorInstance, eventObserver, es, smbService, logger);
		fetchJob.execute(connector);

		assertThatEventsObservedBy(eventObserver).comparingRecordsUsing(es.connectorSmbDocument.url())
				.containsOnly(modifyEvent(es.newConnectorSmbDocument(connectorInstance)
						.setUrl(VALID_SHARE + A_FILENAME)
						.setLastFetchAttemptStatus(LastFetchedStatus.FAILED)));

	}

	private void createDocumentSaveAndAddToElementsPassedToJob(String url, ModelLayerFactory modelLayerFactory)
			throws RecordServicesException {

		connectorSmbDocument = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(url)
				.setConnector(connectorInstance)
				.setTraversalCode(TRAVERSAL_CODE);

		modelLayerFactory.newRecordServices()
				.add(connectorSmbDocument);

		documentsOrFolders.add(connectorSmbDocument);
	}

	private void createFolderSaveAndAddToElementsPassedToJob(String url, ModelLayerFactory modelLayerFactory)
			throws RecordServicesException {

		connectorSmbFolder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(url)
				.setConnector(connectorInstance)
				.setTraversalCode(TRAVERSAL_CODE);

		modelLayerFactory.newRecordServices()
				.add(connectorSmbFolder);

		documentsOrFolders.add(connectorSmbFolder);
	}
}
