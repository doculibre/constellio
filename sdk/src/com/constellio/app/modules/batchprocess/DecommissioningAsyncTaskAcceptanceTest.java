package com.constellio.app.modules.batchprocess;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.ExternalLinkServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningAsyncTask;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Map;
import java.util.UUID;

import static com.constellio.app.modules.rm.model.enums.DecommissioningListType.FOLDERS_TO_DEPOSIT;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DecommissioningAsyncTaskAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	Users users = new Users();
	BatchProcessesManager batchProcessesManager;
	RecordServices recordServices;

	ExternalLinkServices externalLinkServices;

	@Before
	public void setUp() {
		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		recordServices = getModelLayerFactory().newRecordServices();

		externalLinkServices = Mockito.spy(new ExternalLinkServices(zeCollection, getAppLayerFactory()));
	}


	@Test
	public void givenDecommissioningListWithSubFoldersExternallyLinkedThenExternalLinksImportedCorrectly()
			throws Exception {
		ArgumentCaptor<String> externalLinkArg = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> folderIdArg = ArgumentCaptor.forClass(String.class);

		ExternalLink wheatley = rm.newExternalLink().setTitle("WHEATLEY");
		Folder chell = rm.newFolderWithId("chell").setTitle("Chell")
				.setOpenDate(TimeProvider.getLocalDate())
				.setExternalLinks(asList(wheatley.getId()))
				.setParentFolder(records.folder_A01);

		wheatley.setLinkedto(chell);


		ExternalLink companionCube = rm.newExternalLink().setTitle("Companion Cube");
		Folder abeille = rm.getFolder(records.folder_A01)
				.setExternalLinks(asList(companionCube.getId()))
				.setParentFolder(records.folder_A10);

		companionCube.setLinkedto(abeille);

		DecommissioningList glados = rm.newDecommissioningListWithId("glados")
				.setTitle("GLaDOS").setAdministrativeUnit(records.unitId_10a)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT)
				.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setContainerDetailsFor(records.containerId_bac11)
				.setAlreadyIncludedFolderDetailsForIds(records.folder_A10);

		recordServices.execute(new Transaction(wheatley, chell, companionCube, abeille, glados));
		DecommissioningAsyncTask asyncTask = new DecommissioningAsyncTask(zeCollection, users.adminIn(zeCollection).getUsername(), glados.getId()) {
			@Override
			public ExternalLinkServices createExternalLinkServices() {
				return externalLinkServices;
			}
		};
		asyncTask.execute(createAsyncTaskExecutionParams(asyncTask));

		verify(externalLinkServices, times(2)).importExternalLink(externalLinkArg.capture(), folderIdArg.capture());

		assertThat(folderIdArg.getAllValues()).containsOnly(chell.getId(), abeille.getId());
		assertThat(externalLinkArg.getAllValues()).containsOnly(wheatley.getId(), companionCube.getId());
	}

	protected AsyncTaskExecutionParams createAsyncTaskExecutionParams(AsyncTask task) {
		return new AsyncTaskExecutionParams() {
			@Override
			public String getCollection() {
				return zeCollection;
			}

			@Override
			public void logWarning(String code, Map<String, Object> parameters) {
			}

			@Override
			public void logError(String code, Map<String, Object> parameters) {
			}

			@Override
			public void incrementProgression(int numberToAdd) {
			}

			@Override
			public void resetProgression() {
			}

			@Override
			public void setProgressionUpperLimit(long progressionUpperLimit) {
			}

			@Override
			public AsyncTaskBatchProcess getBatchProcess() {
				return new AsyncTaskBatchProcess(UUID.randomUUID().toString(), BatchProcessStatus.PENDING,
						TimeProvider.getLocalDateTime(), TimeProvider.getLocalDateTime(), 0, task, admin,
						"DecommissioningAsyncTaskAcceptanceTest", zeCollection);
			}
		};
	}

}
