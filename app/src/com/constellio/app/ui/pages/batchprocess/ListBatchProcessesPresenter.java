package com.constellio.app.ui.pages.batchprocess;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.BatchProcessVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.BatchProcessToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.BatchProcessDataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;

public class ListBatchProcessesPresenter extends BasePresenter<ListBatchProcessesView> {

	private int secondsSinceLastRefresh = 0;

	private BatchProcessToVOBuilder voBuilder;

	private BatchProcessDataProvider userBatchProcessDataProvider = new BatchProcessDataProvider();
	private BatchProcessDataProvider systemBatchProcessDataProvider = new BatchProcessDataProvider();

	private Map<String, BatchProcessReport> batchProcessReports = new HashMap<>();

	private RecordToVOBuilder recordToVOBuilder;

	public ListBatchProcessesPresenter(ListBatchProcessesView view) {
		super(view);
		voBuilder = new BatchProcessToVOBuilder(appLayerFactory.getModelLayerFactory().getBatchProcessesManager());
		init();
	}

	private void init() {
		refreshDataProviders();
		recordToVOBuilder = new RecordToVOBuilder();
		view.setUserBatchProcesses(userBatchProcessDataProvider);
		List<Record> records = searchServices().search(new LogicalSearchQuery().setCondition(
				fromEveryTypesOfEveryCollection().where(Schemas.SCHEMA).isEqualTo(BatchProcessReport.FULL_SCHEMA)));

		if (records != null) {
			for (Record record : records) {
				BatchProcessReport report = new SchemasRecordsServices(record.getCollection(), modelLayerFactory).wrapBatchProcessReport(record);
				this.batchProcessReports.put(report.getLinkedBatchProcess(), report);
			}
		}

		if (hasSystemBatchProcessAccessInAnyCollection()) {
			view.setSystemBatchProcesses(systemBatchProcessDataProvider);
		}
	}

	private void refreshDataProviders() {
		userBatchProcessDataProvider.clear();
		systemBatchProcessDataProvider.clear();

		String currentUsername = ConstellioUI.getCurrentSessionContext().getCurrentUser().getUsername();

		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		List<BatchProcess> displayedBatchProcesses = new ArrayList<>();

		LocalDateTime oldestAgeDisplayed = new LocalDateTime().minus(Hours.FIVE);
		List<BatchProcess> finishedBatchProcesses = batchProcessesManager.getFinishedBatchProcesses();
		for (BatchProcess batchProcess : finishedBatchProcesses) {
			if (oldestAgeDisplayed.isBefore(batchProcess.getStartDateTime())) {
				displayedBatchProcesses.add(batchProcess);
			}
		}

		BatchProcess currentBatchProcess = batchProcessesManager.getCurrentBatchProcess();
		if (currentBatchProcess != null) {
			displayedBatchProcesses.add(currentBatchProcess);
		}
		List<BatchProcess> pendingBatchProcesses = batchProcessesManager.getPendingBatchProcesses();
		displayedBatchProcesses.addAll(pendingBatchProcesses);
		displayedBatchProcesses.sort(Comparator.comparing(BatchProcess::getRequestDateTime).reversed());

		Map<String, Boolean> collectionAccess = new HashMap<>();
		for (int i = 0; i < displayedBatchProcesses.size(); i++) {
			BatchProcess batchProcess = displayedBatchProcesses.get(i);
			String collection = batchProcess.getCollection();

			if (!collectionAccess.containsKey(collection)) {
				collectionAccess.put(collection, hasSystemBatchProcessAccess(collection));
			}

			BatchProcessVO batchProcessVO = voBuilder.build(batchProcess);
			if (collectionAccess.get(collection)) {
				systemBatchProcessDataProvider.addBatchProcess(batchProcessVO);
			}

			String batchProcessUsername = batchProcessVO.getUsername();
			if (currentUsername.equals(batchProcessUsername)) {
				userBatchProcessDataProvider.addBatchProcess(batchProcessVO);
			}
		}

		userBatchProcessDataProvider.fireDataRefreshEvent();
		systemBatchProcessDataProvider.fireDataRefreshEvent();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).onSomething();
	}

	private boolean hasSystemBatchProcessAccess(String collection) {
		UserServices userServices = modelLayerFactory.newUserServices();
		String username = getCurrentUser().getUsername();

		User userInCollection;
		try {
			userInCollection = userServices.getUserInCollection(username, collection);
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
			return false;
		}

		return userInCollection.has(CorePermissions.VIEW_SYSTEM_BATCH_PROCESSES).globally();
	}

	private boolean hasSystemBatchProcessAccessInAnyCollection() {
		CollectionsManager collectionManager = appLayerFactory.getCollectionsManager();

		List<String> collections = collectionManager.getCollectionCodes();
		for (String collection : collections) {
			if (hasSystemBatchProcessAccess(collection)) {
				return true;
			}
		}

		return false;
	}

	public void backgroundViewMonitor() {
		secondsSinceLastRefresh++;
		if (secondsSinceLastRefresh >= 10) {
			refreshDataProviders();
			secondsSinceLastRefresh = 0;
		}
	}

	public BatchProcessReport getBatchProcessReport(String id) {
		return batchProcessReports.get(id);
	}

	public RecordVO getBatchProcessReportVO(String id) {
		BatchProcessReport report = batchProcessReports.get(id);
		if (report != null) {
			return recordToVOBuilder.build(report.getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
		} else {
			return null;
		}
	}

	public void showErrorMessage(String message) {
		view.showErrorMessage(message);
	}
}