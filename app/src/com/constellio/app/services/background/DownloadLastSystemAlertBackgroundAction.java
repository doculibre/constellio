package com.constellio.app.services.background;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder.RecordScript;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.SchemasRecordsServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class DownloadLastSystemAlertBackgroundAction implements Runnable {
	private String oldAlertHash;
	private String newAlertHash;
	private File newAlert;

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private SchemasRecordsServices schemasRecordsServices;
	private SystemConfigurationsManager manager;

	public DownloadLastSystemAlertBackgroundAction(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.schemasRecordsServices = SchemasRecordsServices.usingMainModelLayerFactory(Collection.SYSTEM_COLLECTION, modelLayerFactory);
		this.manager = modelLayerFactory.getSystemConfigurationsManager();

		try {
			getOldAlertHash();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getOldAlertHash() throws IOException {
		File lastAlertFromValue = manager.getFileFromValue(ConstellioEIMConfigs.LOGIN_NOTIFICATION_STATE_ALERT, "lastAlert.pdf");

		if (null != lastAlertFromValue) {
			oldAlertHash = calculateFileAlertHash(lastAlertFromValue);
		}
	}

	@Override
	public void run() {
		System.out.println("DownloadLastSystemAlertBackgroundAction @ " + new Date());

		downloadLastAlertFromServer();

		if (null != newAlert) {
			try {
				newAlertHash = calculateFileAlertHash(newAlert);
			} catch (IOException e) { //TODO handle better
				e.printStackTrace();
			}

			if (!isOldAndNewAlertHashEquals()) {
				resetHasReadLastAlertMetadataOnUsers();
				copyNewAlertFileToConfigValue();
				oldAlertHash = newAlertHash;
				newAlertHash = null;
			}
		}
	}

	private void downloadLastAlertFromServer() {
		//TODO get file from update server
		newAlert = new File("C:\\Users\\Michael\\Desktop\\UserManual.pdf");

		if (!newAlert.exists()) {
			newAlert = null;
		}
	}

	private String calculateFileAlertHash(File fileToHash) throws IOException {
		ContentManager contentManager = new ContentManager(modelLayerFactory);
		return contentManager.upload(fileToHash).getHash();
	}

	private boolean isOldAndNewAlertHashEquals() {
		return newAlertHash.equals(oldAlertHash);
	}

	private void resetHasReadLastAlertMetadataOnUsers() {
		new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, appLayerFactory.getModelLayerFactory()
				.getUserCredentialsManager().getUserCredentialsWithReadLastAlert().getCondition())
				.setOptions(RecordUpdateOptions
						.validationExceptionSafeOptions())
				.modifyingRecordsWithImpactHandling(new RecordScript() {

					@Override
					public void modifyRecord(Record record) {
						UserCredential userCredential = schemasRecordsServices.wrapUserCredential(record);
						userCredential.setReadLastAlert(false);
					}
				});
	}

	private void copyNewAlertFileToConfigValue() {
		StreamFactory<InputStream> streamFactory = new StreamFactory<InputStream>() {
			@Override
			public InputStream create(String name)
					throws IOException {
				return new FileInputStream(newAlert.getPath());
			}
		};

		manager.setValue(ConstellioEIMConfigs.LOGIN_NOTIFICATION_STATE_ALERT, streamFactory);
		newAlert = null;
	}
}
