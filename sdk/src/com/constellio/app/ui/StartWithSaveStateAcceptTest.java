package com.constellio.app.ui;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.PreserveState;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
@MainTest
@PreserveState(state = "C:\\Users\\Patrick\\Downloads\\systemstate(6).zip", enabled = false)
public class StartWithSaveStateAcceptTest extends ConstellioTest {

	@Test
	@MainTestDefaultStart
	public void startApplicationWithSaveState()
			throws Exception {
		givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();

		File stateFile = new File(getClass().getAnnotation(PreserveState.class).state());
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(stateFile).withPasswordsReset()
				.withFakeEncryptionServices();

		AppLayerFactory appLayerFactory = getAppLayerFactory();
		//		appLayerFactory.getExtensions().getSystemWideExtensions().pagesComponentsExtensions
		//				= new TestPagesComponentsExtensions(appLayerFactory);

		//getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);
		//waitForBatchProcess();

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);
		newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	private SecondTransactionLogReplayFilter withoutFoldersDocumentsEventsAndIndexes() {
		return new SecondTransactionLogReplayFilter() {

			Set<String> okIds = new HashSet<>();

			@Override
			public boolean isReplayingAdd(String id, String schema, SolrInputDocument solrInputDocument) {
				if ((schema != null && (schema.startsWith("event_") || schema.startsWith("folder_")
						|| schema.startsWith("document_")))) {
					return false;
				} else {
					okIds.add(id);
					return true;
				}
			}

			@Override
			public boolean isReplayingUpdate(String id, SolrInputDocument solrInputDocument) {
				return okIds.contains(id);
			}
		};
	}

	private SecondTransactionLogReplayFilter withoutEvents() {
		return new SecondTransactionLogReplayFilter() {

			Set<String> okIds = new HashSet<>();

			@Override
			public boolean isReplayingAdd(String id, String schema, SolrInputDocument solrInputDocument) {
				if (schema != null && (schema.startsWith("folder_") || schema.startsWith("document_"))) {
					return false;
				} else {
					okIds.add(id);
					return true;
				}
			}

			@Override
			public boolean isReplayingUpdate(String id, SolrInputDocument solrInputDocument) {
				return okIds.contains(id);
			}
		};
	}

	@Test
	@MainTestDefaultStart
	public void loadSaveStateUpdateReindexAndProduceNewSaveState()
			throws Exception {
		givenTransactionLogIsEnabled();
		ensurePreserveStateIsNotEnabled();
		File stateFile = new File(getClass().getAnnotation(PreserveState.class).state());
		File updatedSaveState;
		if (stateFile.isDirectory()) {
			updatedSaveState = new File(stateFile.getParentFile(), stateFile.getName() + "-updated.zip");
		} else {
			updatedSaveState = new File(stateFile.getParentFile(), stateFile.getName().replace(".zip", "-updated.zip"));
		}
		updatedSaveState.delete();
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(stateFile);
		waitForBatchProcess();

		getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);
		waitForBatchProcess();

		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();

		System.out.println("Creating new save state...");
		File tempContentFolder = getDataLayerFactory().getDataLayerConfiguration().getContentDaoFileSystemFolder();
		File tempSettingsFolder = getDataLayerFactory().getDataLayerConfiguration().getSettingsFileSystemBaseFolder();

		File tempFolder = newTempFolder();
		File settingsFolder = new File(tempFolder, "settings");
		File contentFolder = new File(tempFolder, "content");

		FileUtils.deleteDirectory(new File(tempContentFolder, "tlogs_bck"));

		FileUtils.copyDirectory(tempContentFolder, contentFolder);
		FileUtils.copyDirectory(tempSettingsFolder, settingsFolder);

		File zipFile = new File(newTempFolder(), "state.zip");

		getIOLayerFactory().newZipService().zip(zipFile, asList(settingsFolder, contentFolder));

		FileUtils.moveFile(zipFile, updatedSaveState);

		System.out.println("Finished!!");
		System.out.println("Your savestate is available here '" + updatedSaveState.getAbsolutePath() + "'");

	}

	private void ensurePreserveStateIsNotEnabled() {
		if (StartWithSaveStateAcceptTest.class.getAnnotation(PreserveState.class).enabled()) {
			throw new RuntimeException("Must disable state preservation (set enabled to false)");
		}

	}
}
