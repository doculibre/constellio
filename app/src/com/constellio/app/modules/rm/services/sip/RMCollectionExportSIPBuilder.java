package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.record.RecordSIPWriter;
import com.constellio.app.services.sip.zip.AutoSplittedSIPZipWriter;
import com.constellio.app.services.sip.zip.AutoSplittedSIPZipWriter.AutoSplittedSIPZipWriterListener;
import com.constellio.app.services.sip.zip.DefaultSIPFileNameProvider;
import com.constellio.app.services.sip.zip.SIPFileNameProvider;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.event.AutoSplitByDayEventsExecutor;
import com.constellio.model.services.event.DayProcessedEvent;
import com.constellio.model.services.event.DayProcessedListener;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.modules.rm.services.sip.RMSIPUtils.buildCategoryDivisionInfos;
import static com.constellio.app.modules.rm.services.sip.RMSIPUtils.buildStorageSpaceInfo;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.apache.commons.io.FileUtils.ONE_GB;

public class RMCollectionExportSIPBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(RMCollectionExportSIPBuilder.class);

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private String collection;

	private RMSchemasRecordsServices rm;
	private TasksSchemasRecordsServices tasks;

	private Locale locale;

	private SearchServices searchServices;

	private User user;

	private File exportFolder;

	private long sipBytesLimit = 2 * ONE_GB;

	private boolean includeContents;
	private IOServices ioServices;

	public RMCollectionExportSIPBuilder(String collection, AppLayerFactory appLayerFactory, File exportFolder) {
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.exportFolder = exportFolder;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.locale = appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionInfo(collection)
				.getMainSystemLocale();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();

	}

	public RMCollectionExportSIPBuilder setIncludeContents(boolean includeContents) {
		this.includeContents = includeContents;
		return this;
	}

	public void exportAllEvents(ProgressInfo progressInfo) {

		final SIPZipWriter sipZipWriter = newSIPZipWriter("events", new HashMap<String, MetsDivisionInfo>(), progressInfo);

		File rootFolder = ioServices.newTemporaryFolder("rootFolder");
		try {
			AutoSplitByDayEventsExecutor autoSplitByDayEventsExecutor = new AutoSplitByDayEventsExecutor(rootFolder,
					appLayerFactory.getModelLayerFactory());
			autoSplitByDayEventsExecutor.addDateProcessedListener(new DayProcessedListener() {

				@Override
				public void lastDateProcessed(DayProcessedEvent dayProcessedEvent) {
					try {
						sipZipWriter.addToZip(dayProcessedEvent.getFile(), dayProcessedEvent.getPathToFile());
					} catch (IOException e) {
						throw new RuntimeException("Error while adding file to zip", e);
					}
				}
			});

			autoSplitByDayEventsExecutor.wrtieAllEvents();
		} finally {
			sipZipWriter.close();
			ioServices.deleteQuietly(rootFolder);
		}
	}

	public void exportAllContainersBySpace(ProgressInfo progressInfo)
			throws IOException {
		progressInfo.setTask("Exporting containers by boxes in collection '" + collection + "'");
		RecordSIPWriter writer = newRecordSIPWriter("containerByBoxes", buildStorageSpaceInfo(rm), progressInfo);

		writer.setIncludeRelatedMaterials(false);
		writer.setIncludeArchiveDescriptionMetadatasFromODDs(true);

		Set<String> failedExports = new HashSet<>();
		Set<String> exportedContainers = new HashSet<>();
		// Use to avoid same container added multiple time
		Set<String> exportedContainersId = new HashSet<>();

		try {
			progressInfo.setEnd(countContainers());
			SearchResponseIterator<Record> storageSpaceIterator = newRootStorageSpaceIterator();

			while(storageSpaceIterator.hasNext()) {
				storageSpaceProcessing(progressInfo, writer, failedExports, exportedContainers, exportedContainersId, storageSpaceIterator.next());
			}

			storageSpaceProcessOrphan(progressInfo, writer, failedExports, exportedContainers);


		} finally {
			writer.close();
		}

		writeListInFile(failedExports, new File(exportFolder, "info" + File.separator + "failedContainersExport.txt"));
		writeListInFile(exportedContainers, new File(exportFolder, "info" + File.separator + "exportedContainers.txt"));
	}

	private void storageSpaceProcessOrphan(ProgressInfo progressInfo, RecordSIPWriter writer, Set<String> failedExports,
			Set<String> exportedContainers) {
		SearchResponseIterator<Record> searchResponseContainerIterator = newOrphanContainerIterator();
		List<Record> records = new ArrayList<>();

		try {
			while(searchResponseContainerIterator.hasNext()) {
				Record containerFound = searchResponseContainerIterator.next();

				records.add(containerFound);
			}

			for (Record record : records) {
				exportedContainers.add(record.getId());
			}

			writer.add(records);
		} catch (Throwable t) {
			t.printStackTrace();
			failedExports.addAll(new RecordUtils().toIdList(records));
		}


		progressInfo.setCurrentState(progressInfo.getCurrentState() + records.size());
	}

	private void storageSpaceProcessing(ProgressInfo progressInfo, RecordSIPWriter writer, Set<String> failedExports,
			Set<String> exportedContainers, Set<String> exportedId, Record storageSpace) {

		SearchResponseIterator<Record> searchResponseContainerIterator = newChildrenContainerIterator(storageSpace);
		List<Record> records = new ArrayList<>();
		try {
			while(searchResponseContainerIterator.hasNext()) {
				Record containerFound = searchResponseContainerIterator.next();
				if(!exportedId.contains(containerFound.getId())) {
					exportedId.add(containerFound.getId());
					records.add(containerFound);
				}
			}

			for (Record record : records) {
				exportedContainers.add(record.getId());
			}

			writer.add(records);
		} catch (Throwable t) {
			t.printStackTrace();
			failedExports.addAll(new RecordUtils().toIdList(records));
		}

		progressInfo.setCurrentState(progressInfo.getCurrentState() + records.size());

		SearchResponseIterator<Record> storageSpaceChildrenIterator = newStorageSpaceChildrenIterator(storageSpace);

		while(storageSpaceChildrenIterator.hasNext()) {
			Record currentStorageSpace = storageSpaceChildrenIterator.next();
			storageSpaceProcessing(progressInfo, writer, failedExports, exportedContainers, exportedId, currentStorageSpace);
		}
	}

	public void exportAllFoldersAndDocuments(ProgressInfo progressInfo) throws IOException {

		progressInfo.setTask("Exporting folders and documents of collection '" + collection + "'");
		RecordSIPWriter writer = newRecordSIPWriter("foldersAndDocuments", buildCategoryDivisionInfos(rm), progressInfo);
		writer.setIncludeRelatedMaterials(false);
		writer.setIncludeArchiveDescriptionMetadatasFromODDs(true);

		Set<String> failedExports = new HashSet<>();
		Set<String> exportedFolders = new HashSet<>();
		Set<String> exportedDocuments = new HashSet<>();

		try {
			SearchResponseIterator<Record> folderIterator = newRootFoldersIterator();
			progressInfo.setEnd(countFoldersAndDocuments());
			progressInfo.setCurrentState(0);

			while (folderIterator.hasNext()) {
				Record folder = folderIterator.next();
				List<Record> records = new ArrayList<>();
				records.add(folder);
				try {
					SearchResponseIterator<Record> subFoldersIterator = newChildrenIterator(folder);

					while (subFoldersIterator.hasNext()) {
						records.add(subFoldersIterator.next());
					}

					writer.add(records);
					for (Record record : records) {
						if (Folder.SCHEMA_TYPE.equals(record.getTypeCode())) {
							exportedFolders.add(record.getId());
						}
						if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {
							exportedDocuments.add(record.getId());
						}
					}

				} catch (Throwable t) {
					t.printStackTrace();
					failedExports.addAll(new RecordUtils().toIdList(records));
				}

				progressInfo.setCurrentState(progressInfo.getCurrentState() + records.size());
			}

		} finally {
			writer.close();
		}

		writeListInFile(failedExports, new File(exportFolder, "info" + File.separator + "failedFolderExport.txt"));
		writeListInFile(exportedFolders, new File(exportFolder, "info" + File.separator + "exportedFolders.txt"));
		writeListInFile(exportedDocuments, new File(exportFolder, "info" + File.separator + "exportedDocuments.txt"));

	}

	public void exportAllTasks(ProgressInfo progressInfo)
			throws IOException {

		progressInfo.setTask("Exporting tasks of collection '" + collection + "'");
		Map<String, MetsDivisionInfo> divisionInfoMap = getDateMetsDivisionInfoMap();


		RecordSIPWriter writer = newRecordSIPWriter("tasks", divisionInfoMap, progressInfo);
		writer.setIncludeRelatedMaterials(false);
		writer.setIncludeArchiveDescriptionMetadatasFromODDs(true);

		Set<String> failedExports = new HashSet<>();
		Set<String> exportedTasks = new HashSet<>();

		try {
			SearchResponseIterator<Record> tasksIterator = newTasksIterator();
			progressInfo.setEnd(tasksIterator.getNumFound());
			progressInfo.setCurrentState(0);

			while (tasksIterator.hasNext()) {
				Record task = tasksIterator.next();
				try {
					writer.add(task);
					exportedTasks.add(task.getId());

				} catch (Throwable t) {
					t.printStackTrace();
					failedExports.add(task.getId());
				}

				progressInfo.setCurrentState(progressInfo.getCurrentState() + 1);
			}

		} finally {
			writer.close();
		}

		writeListInFile(failedExports, new File(exportFolder, "info" + File.separator + "failedTasksExport.txt"));
		writeListInFile(exportedTasks, new File(exportFolder, "info" + File.separator + "exportedTasks.txt"));

	}

	public static Map<String, MetsDivisionInfo> getDateMetsDivisionInfoMap() {
		Map<String, MetsDivisionInfo> divisionInfoMap = new HashMap<>();
		LocalDate localDate = new LocalDate(2000, 1, 1);
		while (localDate.isBefore(TimeProvider.getLocalDate())) {

			String yearDivId = "_" + localDate.getYear();
			String monthDivId = "_" + localDate.getYear() + "-" + localDate.getMonthOfYear();
			String dayDivId = "_" + localDate.getYear() + "-" + localDate.getMonthOfYear() + "-" + localDate.getDayOfMonth();

			MetsDivisionInfo yearDiv = divisionInfoMap.get(yearDivId);
			if (yearDiv == null) {
				yearDiv = new MetsDivisionInfo(yearDivId, yearDivId, "year");
				divisionInfoMap.put(yearDivId, yearDiv);
			}

			MetsDivisionInfo monthDiv = divisionInfoMap.get(monthDivId);
			if (monthDiv == null) {
				monthDiv = new MetsDivisionInfo(monthDivId, yearDivId, monthDivId, "month");
				divisionInfoMap.put(monthDivId, monthDiv);
			}

			MetsDivisionInfo dayDiv = divisionInfoMap.get(dayDivId);
			if (dayDiv == null) {
				dayDiv = new MetsDivisionInfo(dayDivId, monthDivId, dayDivId, "day");
				divisionInfoMap.put(dayDivId, dayDiv);
			}

			localDate = localDate.plusDays(1);
		}
		return divisionInfoMap;
	}

	private long countFoldersAndDocuments() {
		return searchServices.getResultsCount(from(rm.folder.schemaType(), rm.document.schemaType()).returnAll());
	}

	private void writeListInFile(Set<String> ids, File file)
			throws IOException {
		List<String> sortedIds = new ArrayList<>(ids);
		Collections.sort(sortedIds);

		FileUtils.write(file, StringUtils.join(sortedIds, ", "), "UTF-8");

	}

	private RecordSIPWriter newRecordSIPWriter(String sipName, Map<String, MetsDivisionInfo> divisionInfoMap,
											   final ProgressInfo progressInfo) {
		SIPZipWriter writer = newSIPZipWriter(sipName, divisionInfoMap, progressInfo);
		RMZipPathProvider zipPathProvider = new RMZipPathProvider(rm);
		RecordSIPWriter recordSIPWriter = new RecordSIPWriter(appLayerFactory, writer, zipPathProvider, locale);
		recordSIPWriter.setIncludeContentFiles(includeContents);
		return recordSIPWriter;
	}

	protected SIPZipWriter newSIPZipWriter(String sipName, Map<String, MetsDivisionInfo> divisionInfoMap,
			final ProgressInfo progressInfo) {
		SIPFileNameProvider sipFileNameProvider = new DefaultSIPFileNameProvider(exportFolder, sipName);
		AutoSplittedSIPZipWriter writer = new AutoSplittedSIPZipWriter(appLayerFactory, sipFileNameProvider,
				sipBytesLimit, new DefaultSIPZipBagInfoFactory(appLayerFactory, locale));
		writer.register(new AutoSplittedSIPZipWriterListener() {
			@Override
			public void onSIPFileCreated(String sipName, File file) {
				progressInfo.setProgressMessage("Writing sip file '" + file.getName() + "'");
			}

			@Override
			public void onSIPFileClosed(String sipName, File file, boolean lastFile) {

			}
		});
		//progressInfo.setProgressMessage("Writing sip file '" + sipFileNameProvider.newSIPFile(1).getName() + "'");
		writer.addDivisionsInfoMap(divisionInfoMap);
		return writer;
	}

	private SearchResponseIterator<Record> newTasksIterator() {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.userTask.schemaType())
				.where(tasks.userTask.isModel()).isFalseOrNull());
		query.sortAsc(Schemas.IDENTIFIER);
		return searchServices.recordsIteratorKeepingOrder(query, 1000);
	}

	private SearchResponseIterator<Record> newRootFoldersIterator() {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folder.schemaType())
				.where(rm.folder.parentFolder()).isNull());
		query.sortAsc(rm.folder.categoryCode()).sortAsc(rm.folder.categoryCode());
		return searchServices.recordsIteratorKeepingOrder(query, 1000);
	}

	private SearchResponseIterator<Record> newChildrenIterator(Record folderRecord) {
		return searchServices.recordsIterator(new LogicalSearchQuery(
				fromAllSchemasIn(collection).where(Schemas.PATH_PARTS).isEqualTo(folderRecord.getId())));
	}

	private SearchResponseIterator<Record> newChildrenContainerIterator(Record storageSpace) {
		return searchServices.recordsIterator(new LogicalSearchQuery(
				from(rm.containerRecord.schemaType()).where(rm.containerRecord.storageSpace()).isEqualTo(storageSpace.getId())));
	}

	private SearchResponseIterator<Record> newRootStorageSpaceIterator() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rm.storageSpace.schema()).where(rm.storageSpace.parentStorageSpace()).isNull());
		logicalSearchQuery.sortAsc(Schemas.IDENTIFIER);

		return searchServices.recordsIterator(logicalSearchQuery, 1000);
	}

	private SearchResponseIterator<Record> newStorageSpaceChildrenIterator(Record storageSpaceChildren) {
		return searchServices.recordsIterator(new LogicalSearchQuery(
				from(rm.storageSpace.schema()).where(rm.storageSpace.parentStorageSpace()).isEqualTo(storageSpaceChildren.getId())));
	}

	private SearchResponseIterator<Record> newOrphanContainerIterator() {
		return searchServices.recordsIterator(new LogicalSearchQuery(
				from(rm.containerRecord.schema()).where(rm.containerRecord.storageSpace()).isNull()));
	}

	private long countContainers() {
		return searchServices.getResultsCount(from(rm.containerRecord.schemaType()).returnAll());
	}
}
