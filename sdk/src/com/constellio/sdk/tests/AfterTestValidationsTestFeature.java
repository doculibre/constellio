package com.constellio.sdk.tests;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.recovery.TransactionLogXmlRecoveryManager;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.TimeProvider.DefaultTimeProvider;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.sdk.tests.SolrSDKToolsServices.VaultSnapshot;

import java.io.File;
import java.util.Map;

public class AfterTestValidationsTestFeature {

	private boolean disabledInCurrentTest;

	private FactoriesTestFeatures factoriesTestFeatures;
	private BatchProcessTestFeature batchProcessTestFeature;

	private Map<String, String> sdkProperties;

	private String reindexationMessage =
			"\n\nModifications occured during a reindexation after the test " +
			"triggered by the parameter validateDataIntegrity=true in sdk.properties"
			+ "\nThat means the vault was in a corrupted state or the reindexation broke something.";

	private String tLogReplayMessage =
			"\n\nModifications occured during the replay of the transaction log after the test " +
			"triggered by the parameter validateTransactionLog=true in sdk.properties"
			+ "\nThat means the vault was in a corrupted state or the replay broke something.";

	private String rollbackReplayMessage =
			"\n\nModifications occured during a test that could not be rollbacked after the test, " +
			"triggered by the parameter checkRollback=true in sdk.properties.";
	private VaultSnapshot snapshotBeforeReplay;

	public AfterTestValidationsTestFeature(FileSystemTestFeatures fileSystemTestFeatures,
										   final BatchProcessTestFeature batchProcessTestFeature,
										   final FactoriesTestFeatures factoriesTestFeatures,
										   Map<String, String> sdkProperties) {
		this.factoriesTestFeatures = factoriesTestFeatures;
		this.batchProcessTestFeature = batchProcessTestFeature;
		this.sdkProperties = sdkProperties;

		final File logTempFolder = fileSystemTestFeatures.newTempFolderWithName("tLog");
		if (isValidatingSecondTransactionLog() || isValidatingRollbackLog()) {
			this.factoriesTestFeatures.configure(new DataLayerConfigurationAlteration() {
				@Override
				public void alter(InMemoryDataLayerConfiguration configuration) {
					configuration.setSecondTransactionLogEnabled(true);
					configuration.setSecondTransactionLogBaseFolder(logTempFolder);
				}
			});

		}
	}

	public Throwable afterTest(boolean firstClean, boolean failed) {
		if (!failed && !disabledInCurrentTest) {

			if (factoriesTestFeatures.isInitialized()) {
				ConstellioFactories factories = factoriesTestFeatures
						.getConstellioFactories();
				DataLayerFactory dataLayerFactory = factories.getDataLayerFactory();
				DataLayerConfiguration configuration = dataLayerFactory.getDataLayerConfiguration();
				RecordDao recordDao = dataLayerFactory.newRecordDao();
				SolrSDKToolsServices solrSDKTools = new SolrSDKToolsServices(recordDao);
				if (!firstClean) {
					try {
						if (isValidatingSecondTransactionLog() && configuration.isSecondTransactionLogEnabled()) {
							solrSDKTools.flushAndDeleteContentMarkers();
							validateSecondTransactionLog(solrSDKTools);
						} else if (isValidatingRollbackLog() && configuration.isSecondTransactionLogEnabled()) {
							checkRecovery(solrSDKTools, dataLayerFactory.getTransactionLogXmlRecoveryManager());
						}

						if (isValidatingIntegrity()) {
							solrSDKTools.flushAndDeleteContentMarkers();
							validateIntegrity(solrSDKTools);
						}

					} catch (Throwable t) {
						t.printStackTrace();
						return t;
					}
				}
			}
		}
		return null;
	}

	private void checkRecovery(SolrSDKToolsServices tools,
							   TransactionLogXmlRecoveryManager transactionLogXmlRecoveryManager) {
		if (snapshotBeforeReplay != null) {
			transactionLogXmlRecoveryManager.rollback(null);
			VaultSnapshot currentSnapShot = tools.snapshot();
			tools.ensureSameSnapshots(rollbackReplayMessage, snapshotBeforeReplay, currentSnapShot);
		}
	}

	private void validateSecondTransactionLog(SolrSDKToolsServices tools)
			throws Exception {
		TimeProvider.setTimeProvider(new DefaultTimeProvider());
		DataLayerFactory dataLayerFactory = factoriesTestFeatures.getConstellioFactories().getDataLayerFactory();
		SecondTransactionLogManager secondTransactionLogManager = dataLayerFactory.getSecondTransactionLogManager();
		secondTransactionLogManager.regroupAndMove();

		VaultSnapshot snapshotBeforeReplay = tools.snapshot();
		tools.flushAndDeleteContentMarkers();

		secondTransactionLogManager.destroyAndRebuildSolrCollection();

		tools.ensureSameSnapshots(tLogReplayMessage, snapshotBeforeReplay, tools.snapshot());
	}

	private void validateIntegrity(SolrSDKToolsServices tools)
			throws Exception {

		long recordsCount = tools.getRecordsCount();
		if (recordsCount < 10000) {
			ModelLayerFactory modelLayerFactory = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory();

			VaultSnapshot snapshotBeforeReindexation = tools.snapshot();

			modelLayerFactory.newReindexingServices().reindexCollections(ReindexationMode.RECALCULATE);
			tools.flushAndDeleteContentMarkers();

			tools.ensureSameSnapshots(reindexationMessage, snapshotBeforeReindexation, tools.snapshot());

			modelLayerFactory.newReindexingServices().reindexCollections(ReindexationMode.REWRITE);
			tools.flushAndDeleteContentMarkers();

			tools.ensureSameSnapshots(reindexationMessage, snapshotBeforeReindexation, tools.snapshot());

		}

	}

	private boolean isValidatingIntegrity() {
		return "true".equals(sdkProperties.get("validateDataIntegrity"));
	}

	private boolean isValidatingSecondTransactionLog() {
		return "true".equals(sdkProperties.get("validateTransactionLog"));
	}

	private boolean isValidatingRollbackLog() {
		String validateRollback = sdkProperties.get("validateRollback");
		return validateRollback != null && "true".equals(validateRollback.trim());
	}

	public void disableInCurrentTest() {
		disabledInCurrentTest = true;
	}

	public void startRollbackNow() {
		if (isValidatingRollbackLog() && !disabledInCurrentTest) {

			if (factoriesTestFeatures.isInitialized()) {
				DataLayerFactory dataLayerFactory = factoriesTestFeatures.getConstellioFactories().getDataLayerFactory();
				if (dataLayerFactory.getTransactionLogXmlRecoveryManager().isInRollbackMode()) {
					dataLayerFactory.getTransactionLogXmlRecoveryManager().stopRollbackMode();
				}
				batchProcessTestFeature.waitForAllBatchProcessesAcceptingErrors(null);

				dataLayerFactory.getTransactionLogXmlRecoveryManager().startRollbackMode();
				snapshotBeforeReplay = new SolrSDKToolsServices(dataLayerFactory.newRecordDao()).snapshot();

			}
		}
	}
}
