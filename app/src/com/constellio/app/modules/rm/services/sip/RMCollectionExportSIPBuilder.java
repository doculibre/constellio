package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.record.RecordSIPWriter;
import com.constellio.app.services.sip.zip.AutoSplittedSIPZipWriter;
import com.constellio.app.services.sip.zip.AutoSplittedSIPZipWriter.AutoSplittedSIPZipWriterListener;
import com.constellio.app.services.sip.zip.DefaultSIPFileNameProvider;
import com.constellio.app.services.sip.zip.SIPFileNameProvider;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.modules.rm.services.sip.RMSIPUtils.buildCategoryDivisionInfos;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.apache.commons.io.FileUtils.ONE_GB;

public class RMCollectionExportSIPBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(RMCollectionExportSIPBuilder.class);

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private String collection;

	private RMSchemasRecordsServices rm;

	private Locale locale;

	private SearchServices searchServices;

	private User user;

	private File exportFolder;

	private long sipBytesLimit = 2 * ONE_GB;

	private boolean includeContents;

	public RMCollectionExportSIPBuilder(String collection, AppLayerFactory appLayerFactory, File exportFolder) {
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.exportFolder = exportFolder;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.locale = appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionInfo(collection)
				.getMainSystemLocale();

	}

	public RMCollectionExportSIPBuilder setIncludeContents(boolean includeContents) {
		this.includeContents = includeContents;
		return this;
	}

	public void exportAllFoldersAndDocuments(ProgressInfo progressInfo)
			throws IOException {

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
											   final ProgressInfo progressInfo)
			throws IOException {
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
		RMZipPathProvider zipPathProvider = new RMZipPathProvider(rm);
		RecordSIPWriter recordSIPWriter = new RecordSIPWriter(appLayerFactory, writer, zipPathProvider, locale);
		recordSIPWriter.setIncludeContentFiles(includeContents);
		return recordSIPWriter;
	}

	private SearchResponseIterator<Record> newRootFoldersIterator() {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folder.schemaType())
				.where(rm.folder.parentFolder()).isNull());
		query.sortAsc(rm.folder.categoryCode()).sortAsc(rm.folder.categoryCode());
		return searchServices.recordsIterator(query, 1000);
	}

	private SearchResponseIterator<Record> newChildrenIterator(Record folderRecord) {
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(folderRecord.getCollection())
				.where(rm.folder.parentFolder()).isNull());
		query.sortAsc(Schemas.IDENTIFIER);
		return searchServices.recordsIterator(new LogicalSearchQuery(
				fromAllSchemasIn(collection).where(Schemas.PATH_PARTS).isEqualTo(folderRecord.getId())));
	}

}
