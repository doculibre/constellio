package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.utils.SolrDataUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

public class CompleteSystemStateExporter {

	private ZipService zipService;
	private IOServices ioServices;
	private DataLayerFactory dataLayerFactory;
	private RecordDao recordsDao, eventsDao, notificationsDao;
	private AppLayerFactory appLayerFactory;

	public static final String CLOUD_SYSTEM_STATE_EXPORT_TEMP_FOLDER = "CloudSystemStateExporter_exportCompleteSaveState";

	public CompleteSystemStateExporter(AppLayerFactory appLayerFactory) {
		zipService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newZipService();
		ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();

		dataLayerFactory = appLayerFactory.getModelLayerFactory().getDataLayerFactory();
		recordsDao = dataLayerFactory.newRecordDao();
		eventsDao = dataLayerFactory.newEventsDao();
		notificationsDao = dataLayerFactory.newNotificationsDao();
		this.appLayerFactory = appLayerFactory;
	}

	public InputStream exportCompleteSaveState() throws Exception {
		File tempFolder = ioServices.newTemporaryFolder(CLOUD_SYSTEM_STATE_EXPORT_TEMP_FOLDER);

		try {
			List<File> filesToZip = new ArrayList<>();
			Map<String, RecordDao> recordDaoByName = ImmutableMap.of(
					"records", recordsDao, "events", eventsDao, "notifications", notificationsDao);
			for (Entry<String, RecordDao> entry : recordDaoByName.entrySet()) {
				File outputFile = new File(tempFolder, entry.getKey() + ".output");
				try (OutputStream fos = new FileOutputStream(outputFile);
					 BufferedOutputStream bos = new BufferedOutputStream(fos, 128 * 1024);
					 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos);
					 DataOutputStream dos = new DataOutputStream(gzipOutputStream)) {

					ModifiableSolrParams params = new ModifiableSolrParams()
							.set("sort", "id asc").set("q", "*:*").set("rows", 1000);

					String cursor = CursorMarkParams.CURSOR_MARK_START;
					String oldCursor = null;

					while (!cursor.equals(oldCursor)) {
						params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursor);

						QueryResponse resp = entry.getValue().nativeQuery(params);
						List<SolrDocument> documents = resp.getResults();
						final List<SolrInputDocument> inputDocuments = SolrDataUtils.toInputDocuments(documents);
						for (SolrInputDocument inputDocument : inputDocuments) {
							try {
								String xml = ClientUtils.toXML(inputDocument);
								byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
								dos.writeInt(xmlBytes.length);
								dos.write(xmlBytes);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						oldCursor = cursor;
						cursor = resp.getNextCursorMark();
					}
					dos.writeInt(-1);

					filesToZip.add(outputFile);
				}
			}

			File settingsFolder = new File(tempFolder, "settings");
			dataLayerFactory.getConfigManager().exportTo(settingsFolder);
			filesToZip.add(settingsFolder);

			File contentsFolder = new File(tempFolder, "content");
			new PartialVaultExporter(contentsFolder, appLayerFactory).export(Collections.<String>emptyList());
			filesToZip.add(settingsFolder);

			File zipFile = new File(tempFolder, "completeSavestate.zip");
			zipService.zip(zipFile, filesToZip);

			return new FileInputStream(zipFile);
		} finally {
			ioServices.deleteQuietly(tempFolder);
		}
	}


}
