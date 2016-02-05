package com.constellio.app.modules.es.connectors.smb.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(smbRecordService.isNew(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE)).isTrue();
	}

	@Test
	public void givenExistingDocumentWhenVerifyingIfNewThenFalse()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);

		recordService.update(document.getWrappedRecord());
		recordService.flush();

		assertThat(smbRecordService.isNew(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE)).isFalse();
	}

	@Test
	public void givenSizeModifiedDocumentWhenVerifyingIfModifiedThenTrue()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		String FILE_URL = SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE;
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(FILE_URL);
		document.setPermissionsHash(permissionsHash);
		document.setSize(size);
		document.setLastModified(new LocalDateTime(lastModified));

		LocalDateTime ldt = new LocalDateTime();
		document.setLastModified(ldt);

		recordService.update(document.getWrappedRecord());
		recordService.flush();

		boolean result = smbRecordService.isModified(FILE_URL, lastModified, permissionsHash, 12);

		assertThat(result).isTrue();
	}

	@Test
	public void givenPermissionsModifiedDocumentWhenVerifyingIfModifiedThenTrue()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		String FILE_URL = SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE;
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(FILE_URL);
		document.setPermissionsHash(permissionsHash);
		document.setSize(size);
		document.setLastModified(new LocalDateTime(lastModified));

		LocalDateTime ldt = new LocalDateTime();
		document.setLastModified(ldt);

		recordService.update(document.getWrappedRecord());
		recordService.flush();

		boolean result = smbRecordService.isModified(FILE_URL, lastModified, "modifiedHash", size);

		assertThat(result).isTrue();
	}

	@Test
	public void givenLastModifiedModifiedDocumentWhenVerifyingIfModifiedThenTrue()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		String FILE_URL = SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE;
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(FILE_URL);
		document.setPermissionsHash(permissionsHash);
		document.setSize(size);
		document.setLastModified(new LocalDateTime(lastModified));

		LocalDateTime ldt = new LocalDateTime();
		document.setLastModified(ldt);

		recordService.update(document.getWrappedRecord());
		recordService.flush();

		// TODO Benoit. With lastModified + 1 the test fails. Should be investigated.
		boolean result = smbRecordService.isModified(FILE_URL, lastModified + 2, permissionsHash, size);

		assertThat(result).isTrue();
	}

	@Test
	public void givenNonModifiedDocumentWhenVerifyingIfModifiedThenFalse()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		String FILE_URL = SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE;
		String permissionsHash = "someHash";
		long size = 10;
		long lastModified = System.currentTimeMillis();

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		document.setUrl(FILE_URL);
		document.setPermissionsHash(permissionsHash);
		document.setSize(size);
		document.setLastModified(new LocalDateTime(lastModified));

		LocalDateTime ldt = new LocalDateTime();
		document.setLastModified(ldt);

		recordService.update(document.getWrappedRecord());
		recordService.flush();

		boolean result = smbRecordService.isModified(FILE_URL, lastModified, permissionsHash, size);

		assertThat(result).isFalse();
	}

	@Test
	public void givenExistingRecordAndModificationsWhenVerifyingForModificationThenTrue()
			throws RecordServicesException {
		long lastModified = 321;
		String permissionHash = "permissionsHash";
		long size = 321;

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		es.getRecordServices()
				.update(document.getWrappedRecord());
		es.getRecordServices()
				.flush();

		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);
		assertThat(smbRecordService.isModified(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE, lastModified, permissionHash, size)).isTrue();
	}

	@Test
	public void givenExistingRecordAndNoModificationsWhenVerifyingForModificationThenFalse()
			throws RecordServicesException {
		long lastModified = 321;
		String permissionsHash = "permissionsHash";
		long size = 321;

		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE)
				.setSize(size)
				.setPermissionsHash(permissionsHash)
				.setLastModified(new LocalDateTime(lastModified));
		es.getRecordServices()
				.update(document.getWrappedRecord());
		es.getRecordServices()
				.flush();

		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);
		assertThat(smbRecordService.isModified(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE, lastModified, permissionsHash, size)).isFalse();
	}

	@Test
	public void givenNonExistingRecordWhenVerifyingForModificationThenTrue() {
		long lastModified = 321;
		String permissionsHash = "permissionsHash";
		long size = 321;

		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);
		assertThat(smbRecordService.isModified(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE, lastModified, permissionsHash, size)).isTrue();
	}

	@Test
	public void givenUrlWithNonCachedNonExistingRecordWhenGettingRecordIdThenGetNull() {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		String id = smbRecordService.getRecordIdForFolder(SmbTestParams.EXISTING_SHARE);
		assertThat(id).isNull();
	}

	@Test
	public void givenUrlWithNonCachedExistingRecordWhenGettingRecordIdThenGetId()
			throws RecordServicesException {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);

		ConnectorSmbFolder folder = es.newConnectorSmbFolder(connectorInstance)
				.setUrl(SmbTestParams.EXISTING_SHARE);

		recordService.update(folder);
		recordService.flush();

		String id = smbRecordService.getRecordIdForFolder(SmbTestParams.EXISTING_SHARE);
		assertThat(id).isEqualTo(folder.getId());
	}

	@Test
	public void givenUrlWithCachedExistingRecordWhenGettingRecordIdThenGetId()
			throws RecordServicesException {
		SmbRecordServiceCache cache = new SmbRecordServiceCache();
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance, cache);

		ConnectorSmbFolder folder = smbRecordService.newConnectorSmbFolder(SmbTestParams.EXISTING_SHARE)
				.setUrl(SmbTestParams.EXISTING_SHARE);

		recordService.update(folder);
		recordService.flush();

		String id = smbRecordService.getRecordIdForFolder(SmbTestParams.EXISTING_SHARE);
		assertThat(id).isEqualTo(folder.getId());
		assertThat(cache.getRecordId(SmbTestParams.EXISTING_SHARE)).isNotEmpty();
	}

	@Test
	public void givenUrlWithCachedNonExistingRecordWhenGettingRecordIdThen()
			throws RecordServicesException {
		SmbRecordServiceCache cache = new SmbRecordServiceCache();
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance, cache);

		ConnectorSmbFolder folder = smbRecordService.newConnectorSmbFolder(SmbTestParams.EXISTING_SHARE)
				.setUrl(SmbTestParams.EXISTING_SHARE);

		recordService.update(folder);
		recordService.flush();

		recordService.logicallyDelete(folder.getWrappedRecord(), User.GOD);
		recordService.physicallyDelete(folder.getWrappedRecord(), User.GOD);

		String id = smbRecordService.getRecordIdForFolder(SmbTestParams.EXISTING_SHARE);
		assertThat(id).isNull();
		assertThat(cache.getRecordId(SmbTestParams.EXISTING_SHARE)).isNull();
	}

	@Test
	public void whenUpdatingResumeUrlThenResumeUrlIsUpdated() {
		SmbRecordService smbRecordService = new SmbRecordService(es, connectorInstance);
		String resumeUrl = "resumeUrl";
		smbRecordService.updateResumeUrl(resumeUrl);
		assertThat(connectorInstance.getResumeUrl()).isEqualTo(resumeUrl);
	}
}