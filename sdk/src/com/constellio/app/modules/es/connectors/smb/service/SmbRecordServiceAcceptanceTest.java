package com.constellio.app.modules.es.connectors.smb.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import org.assertj.core.api.Condition;
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
	}

	@Test
	public void givenNewDocumentWhenVerifyingIfNewThenTrue() {

		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		assertThat(smbRecordService.getDocuments(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE)).isEmpty();
	}

	@Test
	public void givenExistingDocumentWhenVerifyingIfNewThenFalse()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);

		recordService.update(document.getWrappedRecord());
		recordService.flush();

		assertThat(smbRecordService.getDocuments(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE)).isNotNull();
	}

	@Test
	public void givenUrlWithNonCachedNonExistingRecordWhenGettingRecordThenGetNull() {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbFolder folder = smbRecordService.getFolderFromCache(SmbTestParams.EXISTING_SHARE, connectorInstance);
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

		String id = smbRecordService.getFolderFromCache(SmbTestParams.EXISTING_SHARE, connectorInstance).getId();
		assertThat(id).isEqualTo(folder.getId());
	}

	@Test
	public void givenUrlNonExistingRecordWhenGettingRecordIdThen()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorDocument folder = smbRecordService.newConnectorDocument(SmbTestParams.EXISTING_SHARE)
				.setUrl(SmbTestParams.EXISTING_SHARE);

		recordService.update(folder);
		recordService.flush();

		recordService.logicallyDelete(folder.getWrappedRecord(), User.GOD);
		recordService.physicallyDelete(folder.getWrappedRecord(), User.GOD);

		ConnectorSmbFolder connectorSmbFolder = smbRecordService.getFolderFromCache(SmbTestParams.EXISTING_SHARE, connectorInstance);
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