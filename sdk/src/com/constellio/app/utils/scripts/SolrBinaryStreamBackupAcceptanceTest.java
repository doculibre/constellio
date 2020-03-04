package com.constellio.app.utils.scripts;

import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.handler.loader.XMLLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SlowTest
public class SolrBinaryStreamBackupAcceptanceTest extends ConstellioTest {

	private String solrUrl;

	private File referenceBackup;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	public SolrBinaryStreamBackupAcceptanceTest() {
	}

	@Before
	public void setUp() throws Exception {
		File configFile = new SDKFoldersLocator().getSDKProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);
		solrUrl = new PropertiesDataLayerConfiguration(configs, null, null, null).getRecordsDaoHttpSolrServerUrl();

		assertThat(StringUtils.isNotBlank(solrUrl)).isTrue();

		//Cleanup cores
		HttpSolrClient recordsClient = new HttpSolrClient.Builder().withBaseSolrUrl(solrUrl + "records").build();
		recordsClient.deleteByQuery("*:*");
		recordsClient.commit();
		recordsClient.close();

		HttpSolrClient eventsClient = new HttpSolrClient.Builder().withBaseSolrUrl(solrUrl + "events").build();
		eventsClient.deleteByQuery("*:*");
		eventsClient.commit();
		eventsClient.close();

		HttpSolrClient notificationsClient = new HttpSolrClient.Builder().withBaseSolrUrl(solrUrl + "notifications").build();
		notificationsClient.deleteByQuery("*:*");
		notificationsClient.commit();
		notificationsClient.close();

		File sdkProject = new FoldersLocator().getSDKProject();
		File resourcesDir = new File(sdkProject, "sdk-resources");
		String pathInResourcesDir = "com/constellio/app/utils/scripts/SolrBackup";
		referenceBackup = new File(resourcesDir, pathInResourcesDir);
		assertThat(referenceBackup.exists());
	}

	@Test
	public void testImportExport() throws Exception {
		String[] importArgs = new String[]{"--import", referenceBackup.getPath(), solrUrl};
		SolrBinaryStreamBackup.main(importArgs);

		File tempOutputDir = newTempFolder();
		String[] outputArgs = new String[]{"--export", tempOutputDir.getPath(), solrUrl};
		SolrBinaryStreamBackup.main(outputArgs);

		assertBackupEqual(new File(tempOutputDir, "records.output"), new File(referenceBackup, "records.output"));
		assertBackupEqual(new File(tempOutputDir, "events.output"), new File(referenceBackup, "events.output"));
		assertBackupEqual(new File(tempOutputDir, "notifications.output"), new File(referenceBackup, "notifications.output"));

		try {
			getModelLayerFactory();
		} catch (Exception e) {
			//Exploding to prevent a failure of the next test
		}

	}

	private void assertBackupEqual(File output1, File output2) throws Exception {

		List<SolrInputDocument> output1Docs = toSolrInputDocumnts(output1);
		List<SolrInputDocument> output2Docs = toSolrInputDocumnts(output2);

		assertThat(output1Docs.size()).isEqualTo(output2Docs.size());

		for (int i = 0; i < output1Docs.size(); i++) {
			SolrInputDocument solrInputDocument1 = output1Docs.get(i);
			SolrInputDocument solrInputDocument2 = output1Docs.get(i);
			assertThat(solrInputDocument1.getFieldNames().size()).isEqualTo(solrInputDocument2.getFieldNames().size());
			for (String fieldName : solrInputDocument1.getFieldNames()) {
				String fieldValue1 = solrInputDocument1.getFieldValues(fieldName).toString();
				String fieldValue2 = solrInputDocument2.getFieldValues(fieldName).toString();
				assertThat(fieldValue1).isEqualTo(fieldValue2);
			}
		}
	}

	private List<SolrInputDocument> toSolrInputDocumnts(File backupFile) throws Exception {
		List<SolrInputDocument> solrInputDocuments = new ArrayList<>();

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLLoader loader = new XMLLoader();

		try (FileInputStream fis = new FileInputStream(backupFile);
			 BufferedInputStream bis = new BufferedInputStream(fis, 128 * 1024);
			 GZIPInputStream gzipInputStream = new GZIPInputStream(bis);
			 DataInputStream dis = new DataInputStream(gzipInputStream)) {
			for (int xmlLength = dis.readInt(); xmlLength > 0; xmlLength = dis.readInt()) {
				byte[] xmlBytes = new byte[xmlLength];
				dis.readFully(xmlBytes);

				XMLStreamReader reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(xmlBytes));
				if (reader.hasNext()) {
					reader.next();
					SolrInputDocument solrInputDocument = loader.readDoc(reader);
					solrInputDocuments.add(solrInputDocument);
				}
			}
		}
		return solrInputDocuments;
	}
}
