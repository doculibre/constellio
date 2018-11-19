package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScriptDeleteAllDansRestart extends ScriptWithLogOutput {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptDeleteAllDansRestart.class);

	public static ScriptParameter CONFIRMATION_PARAMETER = new ScriptParameter(ScriptParameterType.STRING,
			"Delete all constellio settings, delete solr content, delete the vault content and restart the application. Enter (DELETE ALL COLLECTIONS) to confirm.",
			true);

	public static final String DELETE_LOG_FILE = "deletelogfile";

	public ScriptDeleteAllDansRestart(AppLayerFactory appLayerFactory) {
		super(appLayerFactory, "Content", "Delete everything from constellio and restart. ** This cannot be undone.");
	}

	@Override
	protected void execute()
			throws Exception {

		if ("DELETE ALL COLLECTIONS".equals(getParameterValue(CONFIRMATION_PARAMETER))) {
			DataLayerConfiguration dataLayerConfiguration = appLayerFactory.getModelLayerFactory().getDataLayerFactory()
					.getDataLayerConfiguration();

			if (SystemUtils.IS_OS_LINUX) {
				File deleteLogFile = new File("/opt/constellio/systemDeletes.txt");

				if (!deleteLogFile.getParentFile().exists()) {
					deleteLogFile.getParentFile().mkdirs();
				}

				if (!deleteLogFile.exists()) {
					deleteLogFile.createNewFile();

				}

				String format = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_FORMAT);
				OutputStream outputStream = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices()
						.newFileOutputStream(deleteLogFile, DELETE_LOG_FILE);
				try {
					outputStream.write((DateUtils.format(LocalDate.now(), format) + " " + ConstellioUI.getCurrentSessionContext()
							.getCurrentUserIPAddress() + " " + ConstellioUI.getCurrentSessionContext().getCurrentUser()
							.getUsername() + "\n").toString().getBytes());
					outputStream.flush();
				} finally {
					outputStream.close();
				}
			}

			File contentDaoFileSystemFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();

			FileUtils.deleteDirectory(contentDaoFileSystemFolder);
			contentDaoFileSystemFolder.mkdirs();

			File settingsFileSystemBaseFolder = dataLayerConfiguration.getSettingsFileSystemBaseFolder();
			FileUtils.deleteDirectory(settingsFileSystemBaseFolder);

			settingsFileSystemBaseFolder.mkdirs();

			deleteSolrData();

			appLayerFactory.newApplicationService().restart();

			outputLogger.info("Script finished");
		} else {
			outputLogger.info("Script aborted");
		}
	}

	public void deleteSolrData()
			throws Exception {

		File configFile = new FoldersLocator().getConstellioProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);
		String solrUrl = new PropertiesDataLayerConfiguration(configs, null, null, null).getRecordsDaoHttpSolrServerUrl();

		clearDataIn(solrUrl + "records");
		clearDataIn(solrUrl + "events");
		clearDataIn(solrUrl + "notifications");

	}

	private static void clearDataIn(String solrUrl)
			throws Exception {
		SolrClient solrServer = new HttpSolrClient(solrUrl);
		solrServer.deleteByQuery("*:*");
		solrServer.commit();
	}

	@Override
	public List<ScriptParameter> getParameters() {
		return Arrays.asList(CONFIRMATION_PARAMETER);
	}
}
