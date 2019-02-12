package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.record.RecordSIPWriter;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

public class RMSIPBuilder {

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private String collection;

	private RMSchemasRecordsServices rm;

	private Locale locale;

	private SearchServices searchServices;

	private User user;

	public RMSIPBuilder(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.locale = appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionInfo(collection)
				.getMainSystemLocale();
	}

	public Locale getLocale() {
		return locale;
	}

	public RMSIPBuilder setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}

	public User getUser() {
		return user;
	}

	public RMSIPBuilder setUser(User user) {
		this.user = user;
		return this;
	}



	/**
	 * Create an SIP Archive using given folders and document ids
	 */
	public ValidationErrors buildWithFoldersAndDocuments(SIPZipWriter sipZipWriter, List<String> folderIds,
														 List<String> documentIds, ProgressInfo progressInfo)
			throws IOException {

		sipZipWriter.addDivisionsInfoMap(buildCategoryDivisionInfos());

		if (progressInfo == null) {
			progressInfo = new ProgressInfo();
		}

		List<String> ids = getRecordIdsToExport(folderIds, documentIds);
		progressInfo.setEnd(ids.size());

		ValidationErrors errors = new ValidationErrors();
		RecordSIPWriter writer = new RecordSIPWriter(appLayerFactory, sipZipWriter, new RMZipPathProvider(rm), locale);
		try {
			int recordsHandled = 0;

			//TODO : Improve scalability and document/folder grouping

			for (String id : ids) {
				Record record = recordServices.getDocumentById(id);

				if (Folder.SCHEMA_TYPE.equals(record.getTypeCode()) && (user == null || user.hasReadAccess().on(record))) {
					errors.addAll(writer.add(asList(record)).getValidationErrors());

					progressInfo.setCurrentState(recordsHandled + 1);
					recordsHandled++;
				}
			}

			for (String id : ids) {
				Record record = recordServices.getDocumentById(id);
				if (Document.SCHEMA_TYPE.equals(record.getTypeCode()) && (user == null || user.hasReadAccess().on(record))) {
					errors.addAll(writer.add(asList(record)).getValidationErrors());

					progressInfo.setCurrentState(recordsHandled + 1);
					recordsHandled++;
				}
			}

		} finally {
			writer.close();
		}

		return errors;
	}

	@NotNull
	protected List<String> getRecordIdsToExport(List<String> folderIds, List<String> documentIds) {
		Set<String> ids = new HashSet<>();
		ids.addAll(folderIds);
		ids.addAll(documentIds);

		for (String folderId : folderIds) {
			ids.add(folderId);
			ids.addAll(getParentIds(rm.getFolder(folderId)));
			Iterator<String> idsIterator = getChildrenIds(rm.getFolder(folderId));
			while (idsIterator.hasNext()) {
				ids.add(idsIterator.next());
			}
		}

		for (String documentId : documentIds) {
			ids.add(documentId);
			ids.addAll(getParentIds(rm.getDocument(documentId)));
		}


		List<String> orderedIds = new ArrayList<>();
		orderedIds.addAll(ids);
		Collections.sort(orderedIds);
		return orderedIds;
	}

	private Map<String, MetsDivisionInfo> buildCategoryDivisionInfos() {
		Map<String, MetsDivisionInfo> divisionInfoMap = new HashMap<>();
		for (Category category : rm.getAllCategories()) {
			String parentCode = category.getParent() == null ? null : rm.getCategory(category.getParent()).getCode();
			divisionInfoMap.put(category.getCode(), new MetsDivisionInfo(
					category.getCode(), parentCode, category.getTitle(), Category.SCHEMA_TYPE));
		}
		return divisionInfoMap;
	}


	private Iterator<String> getChildrenIds(Folder folder) {
		return searchServices.recordsIdsIterator(new LogicalSearchQuery(
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

}
