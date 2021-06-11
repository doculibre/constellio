package com.constellio.app.modules.es.connectors.smb;

import com.constellio.app.modules.es.connectors.smb.service.SmbFileFactory;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileFactoryImpl;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@InternetTest
public class DeleteConnectorSmbRealTest extends ConstellioTest {

	private ConnectorManager connectorManager;
	private RecordServices recordServices;
	private ESSchemasRecordsServices es;
	private IOServices ioServices;

	private ConnectorSmbInstance connectorInstance;

	ConnectorSmbDocument connectorSmbDocument;
	ConnectorSmbFolder connectorSmbFolder;

	private String share;
	private String domain;
	private String username;
	private String password;

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();
		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();

		share = SDKPasswords.testSmbShare();
		domain = SDKPasswords.testSmbDomain();
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();

		createConnector(share);
		createHierarchyInSmb();
	}

	//broken @Test
	// Confirm @SlowTest
	public void givenConnectorSmbHierarchyWhenDeleteThenOk()
			throws Exception {

		ConnectorSmb connectorSmb = (ConnectorSmb) es.getConnectorManager().instanciate(connectorInstance);
		connectorSmb.setEs(es);
		configureConnectorSmbDocument();
		configureConnectorSmbFolder();
		assertThat(connectorSmb.exists(connectorSmbFolder)).isTrue();
		assertThat(connectorSmb.exists(connectorSmbDocument)).isTrue();
		assertThat(recordServices.getDocumentById(connectorSmbFolder.getId())).isNotNull();
		assertThat(recordServices.getDocumentById(connectorSmbDocument.getId())).isNotNull();

		connectorSmb.deleteFile(connectorSmbDocument);
		assertThat(connectorSmb.exists(connectorSmbDocument)).isFalse();
		connectorSmb.deleteFile(connectorSmbFolder);
		assertThat(connectorSmb.exists(connectorSmbFolder)).isFalse();
		es.getRecordServices().flush();

		assertThat(recordServices.getDocumentById(connectorSmbDocument.getId()).<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS))
				.isEqualTo(
						true);
		assertThat(recordServices.getDocumentById(connectorSmbFolder.getId()).<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS))
				.isEqualTo(
						true);
	}

	private void createHierarchyInSmb()
			throws IOException {
		SmbFileFactory smbFactory = new SmbFileFactoryImpl();
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
				connectorInstance.getDomain(), connectorInstance.getUsername(),
				connectorInstance.getPassword());
		InputStream inputStream1 = null;
		InputStream inputStream2 = null;
		OutputStream outputStream1 = null;
		OutputStream outputStream2 = null;
		try {
			StreamFactory<InputStream> streamFactory = getTestResourceInputStreamFactory("smbFile.txt");
			smbFactory.createSmbFolder(share + "test", auth);
			SmbFile smbFile2 = smbFactory.createSmbFile(share + "test/smbFile2.txt", auth);
			smbFactory.createSmbFolder(share + "test/test1", auth);
			SmbFile smbFile3 = smbFactory.createSmbFile(share + "test/test1/smbFile3.txt", auth);
			smbFactory.createSmbFolder(share + "test/test2", auth);
			outputStream1 = smbFile2.getOutputStream();
			outputStream2 = smbFile3.getOutputStream();
			inputStream1 = streamFactory.create("smbFileInputStream2");
			inputStream2 = streamFactory.create("smbFileInputStream3");
			ioServices.copy(inputStream1, outputStream1);
			ioServices.copy(inputStream2, outputStream2);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.closeQuietly(inputStream1);
			ioServices.closeQuietly(inputStream2);
			ioServices.closeQuietly(outputStream1);
			ioServices.closeQuietly(outputStream2);
		}
	}

	private ConnectorSmbInstance createConnector(String... seeds) {
		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("zeConnectorCode")
				.setEnabled(true)
				.setSeeds(asList(seeds))
				.setUsername(username)
				.setPassword(password)
				.setDomain(domain)
				.setInclusions(Arrays.asList(share))
				.setExclusions(new ArrayList<String>())
				.setTitle("zeConnectorTitle"));

		flushRecord(connectorInstance.getWrappedRecord());

		return connectorInstance;
	}

	private void configureConnectorSmbFolder() {
		boolean ok = false;
		int tryCount = 0;
		while (!ok) {

			Metadata metadata = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
					.getMetadata(ConnectorSmbFolder.SCHEMA_TYPE + "_" + connectorInstance.getId() + "_" + ConnectorSmbFolder.URL);
			LogicalSearchCondition condition = from(es.schemaType(ConnectorSmbFolder.SCHEMA_TYPE))
					.where(metadata).is(share + "test/");

			List<ConnectorSmbFolder> indexedFolders = es
					.searchConnectorSmbFolders(condition);

			if (!indexedFolders.isEmpty()) {
				for (ConnectorSmbFolder indexedFolder : indexedFolders) {
					if (indexedFolder.getUrl().endsWith("test/")) {
						connectorSmbFolder = indexedFolder;
						ok = true;
						break;
					}
				}
			}
			if (tryCount++ > 5000) {
				fail("File not found");
			}
		}
	}

	private void configureConnectorSmbDocument() {
		boolean ok = false;
		int tryCount = 0;
		while (!ok) {
			List<ConnectorSmbDocument> indexedDocuments = es
					.searchConnectorSmbDocuments(es.fromAllDocumentsOf(connectorInstance.getId()));

			if (!indexedDocuments.isEmpty()) {
				for (ConnectorSmbDocument indexedDocument : indexedDocuments) {
					if (indexedDocument.getUrl().endsWith("smbFile2.txt")) {
						System.out.println(indexedDocument.getParentConnectorUrl());
						connectorSmbDocument = indexedDocument;
						ok = true;
						break;
					}
				}
			}
			if (tryCount++ > 5000) {
				fail("File not found");
			}
		}
	}

	private void flushRecord(Record record) {
		try {
			es.getRecordServices()
					.update(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}
}