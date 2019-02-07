package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

public class RMSIPBuilder {

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private String collection;

	private RMSchemasRecordsServices rm;

	private RecordSIPWriter recordSIPWriter;

	private SearchServices searchServices;

	public RMSIPBuilder(String collection,
						AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public ValidationErrors buildWithFoldersAndDocuments(File zipFile, List<String> folderIds, List<String> documentIds,
								  ProgressInfo progressInfo, SIPBuilderParams params)
			throws IOException {

		Map<String, MetsDivisionInfo> divisionInfoMap = new HashMap<>();
		for (Category category : rm.getAllCategories()) {
			String parentCode = category.getParent() == null ? null : rm.getCategory(category.getParent()).getCode();
			MetsDivisionInfo metsDivisionInfo = new MetsDivisionInfo(category.getCode(), parentCode, category.getTitle(), Category.SCHEMA_TYPE);
			divisionInfoMap.put(category.getCode(), metsDivisionInfo);
		}


		if (progressInfo == null) {
			progressInfo = new ProgressInfo();
		}

		RecordSIPWriter writer = new RecordSIPWriter(params, rm.getCollection(), appLayerFactory, zipFile, divisionInfoMap, new RMZipPathProvider());

		//TODO : Improve scalability and document/folder grouping

		Set<String> ids = new HashSet<>();
		ids.addAll(folderIds);
		ids.addAll(documentIds);

		for (String folderId : folderIds) {
			ids.add(folderId);
			ids.addAll(getParentIds(rm.getFolder(folderId)));
			ids.addAll(getChildrenIds(rm.getFolder(folderId)));
		}

		for (String documentId : documentIds) {
			ids.add(documentId);
			ids.addAll(getParentIds(rm.getDocument(documentId)));
		}

		int recordsHandled = 0;
		ValidationErrors errors = new ValidationErrors();

		List<String> orderedIds = new ArrayList<>();
		orderedIds.addAll(ids);
		Collections.sort(orderedIds);

		for (String id : orderedIds) {
			Record record = recordServices.getDocumentById(id);
			if (Folder.SCHEMA_TYPE.equals(record.getTypeCode())) {
				writer.add(asList(record));

				progressInfo.setCurrentState(recordsHandled + 1);
				recordsHandled++;
			}
		}

		for (String id : orderedIds) {
			Record record = recordServices.getDocumentById(id);
			if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {
				writer.add(asList(record));

				progressInfo.setCurrentState(recordsHandled + 1);
				recordsHandled++;
			}
		}

		writer.close();

		return errors;
	}

	private List<String> getChildrenIds(Folder folder) {
		return searchServices.searchRecordIds(new LogicalSearchQuery(
				fromAllSchemasIn(collection).where(Schemas.PATH_PARTS).isEqualTo(folder.getId())));
	}

	private List<String> getParentIds(Folder folder) {
		List<String> parentIds = new ArrayList<>();
		if (folder.getParentFolder() != null) {
			Folder parentFolder = rm.getFolder(folder.getParentFolder());
			parentIds.add(parentFolder.getId());
			parentIds.addAll(getParentIds(parentFolder));
		}

		return parentIds;
	}

	private List<String> getParentIds(Document document) {
		List<String> parentIds = new ArrayList<>();
		Folder parentFolder = rm.getFolder(document.getFolder());
		parentIds.add(parentFolder.getId());
		parentIds.addAll(getParentIds(parentFolder));
		return parentIds;
	}

	private class RMZipPathProvider implements Provider<Record, String> {


		@Override
		public String get(Record record) {
			if (Category.SCHEMA_TYPE.equals(record.getTypeCode())) {
				Category currentCategory = rm.wrapCategory(record);
				if (currentCategory.getParent() != null) {
					return get(recordServices.getDocumentById(currentCategory.getParent())) + "/" + currentCategory.getCode();
				} else {
					return "/data/" + currentCategory.getCode();
				}

			} else if (Folder.SCHEMA_TYPE.equals(record.getTypeCode())) {
				Folder folder = rm.wrapFolder(record);
				if (folder.getParentFolder() != null) {
					return get(recordServices.getDocumentById(folder.getParentFolder())) + "/" + folder.getId();
				} else {
					return get(recordServices.getDocumentById(folder.getCategory())) + "/" + folder.getId();
				}

			} else if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {
				Document document = rm.wrapDocument(record);
				return get(recordServices.getDocumentById(document.getFolder())) + "/" + document.getId();

			} else {
				return "/data/" + record.getId();
			}
		}
	}

}
