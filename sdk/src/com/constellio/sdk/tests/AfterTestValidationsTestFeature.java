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
package com.constellio.sdk.tests;

import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.Map;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.TimeProvider.DefaultTimeProvider;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.sdk.tests.SolrSDKToolsServices.VaultSnapshot;

public class AfterTestValidationsTestFeature {

	private boolean disabledInCurrentTest;

	private FactoriesTestFeatures factoriesTestFeatures;

	private Map<String, String> sdkProperties;

	private String reindexationMessage =
			"\n\nModifications occured during a reindexation after the test " +
					"triggered by the parameter validateDataIntegrity=true in sdk.properties"
					+ "\nThat means the vault was in a corrupted state or the reindexation broke something.";

	private String tLogReplayMessage =
			"\n\nModifications occured during the replay of the transaction log after the test " +
					"triggered by the parameter validateTransactionLog=true in sdk.properties"
					+ "\nThat means the vault was in a corrupted state or the replay broke something.";

	public AfterTestValidationsTestFeature(FileSystemTestFeatures fileSystemTestFeatures,
			final FactoriesTestFeatures factoriesTestFeatures, Map<String, String> sdkProperties) {
		this.factoriesTestFeatures = factoriesTestFeatures;
		this.sdkProperties = sdkProperties;

		final File logTempFolder = fileSystemTestFeatures.newTempFolderWithName("tLog");
		if (isValidatingSecondTransactionLog()) {
			this.factoriesTestFeatures.configure(new DataLayerConfigurationAlteration() {
				@Override
				public void alter(DataLayerConfiguration configuration) {
					doReturn(true).when(configuration).isSecondTransactionLogEnabled();
					doReturn(logTempFolder).when(configuration).getSecondTransactionLogBaseFolder();
				}
			});
		}
	}

	public Throwable afterTest(boolean firstClean, boolean failed) {
		if (!failed && !disabledInCurrentTest) {
			DataLayerFactory dataLayerFactory = factoriesTestFeatures.getConstellioFactories().getDataLayerFactory();
			DataLayerConfiguration configuration = dataLayerFactory.getDataLayerConfiguration();
			RecordDao recordDao = dataLayerFactory.newRecordDao();
			SolrSDKToolsServices solrSDKTools = new SolrSDKToolsServices(recordDao);
			if (!firstClean) {
				try {
					if (isValidatingSecondTransactionLog() && configuration.isSecondTransactionLogEnabled()) {
						solrSDKTools.flushAndDeleteContentMarkers();
						validateSecondTransactionLog(solrSDKTools);

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
		return null;
	}

	private void validateSecondTransactionLog(SolrSDKToolsServices tools)
			throws Exception {
		TimeProvider.setTimeProvider(new DefaultTimeProvider());
		DataLayerFactory dataLayerFactory = factoriesTestFeatures.getConstellioFactories().getDataLayerFactory();
		SecondTransactionLogManager secondTransactionLogManager = dataLayerFactory.getSecondTransactionLogManager();
		secondTransactionLogManager.regroupAndMoveInVault();

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

	public void disableInCurrentTest() {
		disabledInCurrentTest = true;
	}
}
