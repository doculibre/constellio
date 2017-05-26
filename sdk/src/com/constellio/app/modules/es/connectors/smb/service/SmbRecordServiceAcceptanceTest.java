package com.constellio.app.modules.es.connectors.smb.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContextServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SmbRecordServiceAcceptanceTest extends ConstellioTest {
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private RecordServices recordService;
	private SmbConnectorContext context;

	@Before
	public void setup()
			throws RecordServicesException {
		prepareSystem(withZeCollection().withConstellioESModule()
				.withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordService = es.getRecordServices();

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

		recordService.update(connectorInstance);
		recordService.flush();

		es.getConnectorManager()
				.createConnector(connectorInstance);


		SmbConnectorContextServices contextServices = new SmbConnectorContextServices(es);
		context = contextServices.createContext(connectorInstance.getId());
	}

	@Test
	public void givenNewDocumentWhenVerifyingIfNewThenTrue() {

		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		assertThat(smbRecordService.getDocument(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE)).isNull();
	}

	@Test
	public void givenExistingDocumentWhenVerifyingIfNewThenFalse()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);

		recordService.update(document.getWrappedRecord());
		recordService.flush();

		assertThat(smbRecordService.getDocument(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE)).isNotNull();
	}

	@Test
	public void givenUrlWithNonCachedNonExistingRecordWhenGettingRecordThenGetNull() {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbFolder folder = smbRecordService.getFolder(SmbTestParams.EXISTING_SHARE);
		assertThat(folder).isNull();
	}

	@Test
	public void givenUrlWithNonCachedExistingRecordWhenGettingRecordIdThenGetId()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbFolder folder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE);

		recordService.update(folder);
		recordService.flush();

		String id = smbRecordService.getFolder(SmbTestParams.EXISTING_SHARE).getId();
		assertThat(id).isEqualTo(folder.getId());
	}

	@Test
	public void givenUrlNonExistingRecordWhenGettingRecordIdThen()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbFolder folder = smbRecordService.newConnectorSmbFolder(SmbTestParams.EXISTING_SHARE)
				.setUrl(SmbTestParams.EXISTING_SHARE);

		recordService.update(folder);
		recordService.flush();

		recordService.logicallyDelete(folder.getWrappedRecord(), User.GOD);
		recordService.physicallyDelete(folder.getWrappedRecord(), User.GOD);

		ConnectorSmbFolder connectorSmbFolder = smbRecordService.getFolder(SmbTestParams.EXISTING_SHARE);
		assertThat(connectorSmbFolder).isNull();
	}

	@Test
	public void whenUpdatingResumeUrlThenResumeUrlIsUpdated() {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);
		String resumeUrl = "resumeUrl";
		smbRecordService.updateResumeUrl(resumeUrl);
		assertThat(connectorInstance.getResumeUrl()).isEqualTo(resumeUrl);
	}

	@Test
	public void givenDuplicateFoldersThenGetDuplicates()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbFolder folder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE);

		ConnectorSmbFolder folder2 = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE);

		ConnectorSmbFolder folder3 = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + "/test/");

		recordService.add(folder);
		recordService.add(folder2);
		recordService.add(folder3);
		recordService.flush();

		assertThat(smbRecordService.getFolders(SmbTestParams.EXISTING_SHARE)).hasSize(2);

		Set<String> urls = smbRecordService.duplicateDocuments();
		assertThat(urls).containsExactly(SmbTestParams.EXISTING_SHARE);
	}

	@Test
	public void givenDuplicateDocumentsThenGetDuplicates()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);

		ConnectorSmbDocument document2 = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);

		ConnectorSmbDocument document3 = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.ANOTHER_FILE_NAME);

		recordService.add(document);
		recordService.add(document2);
		recordService.add(document3);
		recordService.flush();

		assertThat(smbRecordService.getDocuments(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE)).hasSize(2);

		Set<String> urls = smbRecordService.duplicateDocuments();
		assertThat(urls).containsExactly(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
	}

	@Test
	public void givenFoldersThenPathOk()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbFolder folderPartial = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + "/testPartial/").setLastFetchedStatus(LastFetchedStatus.PARTIAL);

		ConnectorSmbFolder folderFailed = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + "/testFailed/").setLastFetchedStatus(LastFetchedStatus.FAILED);

		ConnectorSmbFolder folderOk = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + "/testOk/").setLastFetchedStatus(LastFetchedStatus.OK);

		recordService.add(folderPartial);
		recordService.add(folderFailed);
		recordService.add(folderOk);
		recordService.flush();

		assertThat(smbRecordService.getFolders(SmbTestParams.EXISTING_SHARE + "/testPartial/").get(0).get(CommonMetadataBuilder.PATH_PARTS)).isEqualTo(Arrays.asList());
		assertThat(smbRecordService.getFolders(SmbTestParams.EXISTING_SHARE + "/testFailed/").get(0).get(CommonMetadataBuilder.PATH_PARTS)).isEqualTo(Arrays.asList());
		assertThat((List<String>) smbRecordService.getFolders(SmbTestParams.EXISTING_SHARE + "/testOk/").get(0).get(CommonMetadataBuilder.PATH_PARTS)).has(new Condition<List<String>>() {
			@Override
			public boolean matches(List<String> value) {
				return value.get(0).contains("R");
			}
		});
	}
}