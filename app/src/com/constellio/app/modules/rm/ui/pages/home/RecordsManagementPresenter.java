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
package com.constellio.app.modules.rm.ui.pages.home;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.model.enums.StartTab;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentEventSchemaToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderEventSchemaToVOBuilder;
import com.constellio.app.modules.rm.ui.pages.home.RecordsManagementView.RecordsManagementViewTab;
import com.constellio.app.modules.rm.ui.pages.home.RecordsManagementView.TabType;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class RecordsManagementPresenter extends BasePresenter<RecordsManagementView> {

	private static final String TAB_NAME_LAST_VIEWED_FOLDERS = "lastViewedFolders";
	private static final String TAB_NAME_LAST_VIEWED_DOCUMENTS = "lastViewedDocuments";
	private static final String TAB_NAME_TAXONOMIES = "taxonomies";

	private RecordsManagementViewTab tabLastViewedFolders;
	private RecordsManagementViewTab tabLastViewedDocuments;
	private RecordsManagementViewTab tabTaxonomies;

	private List<RecordsManagementViewTab> tabs;

	private FolderEventSchemaToVOBuilder folderEventSchemaToVOBuilder = new FolderEventSchemaToVOBuilder();

	private DocumentEventSchemaToVOBuilder documentEventSchemaToVOBuilder = new DocumentEventSchemaToVOBuilder();

	private RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();

	private MetadataSchemaVO foldersSchemaVO;

	private MetadataSchemaVO documentsSchemaVO;

	private transient ModelLayerFactory modelLayerFactory;

	private transient MetadataSchemasManager metadataSchemasManager;

	private transient MetadataSchemaTypes schemaTypes;

	public RecordsManagementPresenter(RecordsManagementView view) {
		super(view);
		init();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		modelLayerFactory = constellioFactories.getModelLayerFactory();

		SessionContext sessionContext = view.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		schemaTypes = metadataSchemasManager.getSchemaTypes(collection);

		MetadataSchema foldersSchema = getFoldersSchema();
		MetadataSchema documentsSchema = getDocumentsSchema();

		if (foldersSchema != null) {
			foldersSchemaVO = folderEventSchemaToVOBuilder.build(foldersSchema, VIEW_MODE.TABLE);
			documentsSchemaVO = documentEventSchemaToVOBuilder.build(documentsSchema, VIEW_MODE.TABLE);

			tabLastViewedFolders = new RecordsManagementViewTab(
					TAB_NAME_LAST_VIEWED_FOLDERS,
					TabType.RECORD_LIST,
					getDataProviders(TAB_NAME_LAST_VIEWED_FOLDERS), false);
			tabLastViewedDocuments = new RecordsManagementViewTab(
					TAB_NAME_LAST_VIEWED_DOCUMENTS,
					TabType.RECORD_LIST,
					getDataProviders(TAB_NAME_LAST_VIEWED_DOCUMENTS), true);
			tabTaxonomies = new RecordsManagementViewTab(
					TAB_NAME_TAXONOMIES,
					TabType.RECORD_TREE,
					getDataProviders(TAB_NAME_TAXONOMIES), true);

			tabs = Arrays.asList(tabLastViewedFolders, tabLastViewedDocuments, tabTaxonomies);
		} else {
			tabs = new ArrayList<>();
		}
	}

	private List<DataProvider> getDataProviders(String tabName) {
		List<DataProvider> dataProviders = new ArrayList<DataProvider>();
		if (TAB_NAME_LAST_VIEWED_FOLDERS.equals(tabName)) {
			DataProvider dataProvider = new RecordVODataProvider(foldersSchemaVO, recordToVOBuilder, modelLayerFactory) {
				@Override
				protected LogicalSearchQuery getQuery() {
					return recentEventsQuery(Folder.SCHEMA_TYPE, "view_folder");
				}
			};
			dataProviders.add(dataProvider);
		} else if (TAB_NAME_LAST_VIEWED_DOCUMENTS.equals(tabName)) {
			DataProvider dataProvider = new RecordVODataProvider(documentsSchemaVO, recordToVOBuilder, modelLayerFactory) {
				@Override
				protected LogicalSearchQuery getQuery() {
					return recentEventsQuery(Document.SCHEMA_TYPE, "view_document");
				}
			};
			dataProviders.add(dataProvider);
		} else {
			for (String taxonomyCode : getAllTaxonomyCodes()) {
				DataProvider dataProvider = new RecordLazyTreeDataProvider(taxonomyCode);
				dataProviders.add(dataProvider);
			}
		}
		return dataProviders;
	}

	private LogicalSearchQuery recentEventsQuery(String typeCode, String eventType) {

		List<Event> events = fetchEvents(eventType, 50);
		List<String> recordIds = new ArrayList<>();

		Map<String, LocalDateTime> eventsViewDateTimes = new HashMap<>();
		for (Event event : events) {
			if (!eventsViewDateTimes.containsKey(event.getRecordId())) {
				recordIds.add(event.getRecordId());
				eventsViewDateTimes.put(event.getRecordId(), event.getModifiedOn());
			}
		}

		MetadataSchemaType type = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(typeCode);
		return new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(type).where(Schemas.IDENTIFIER).isIn(recordIds))
				.setResultsProjection(new SortRecordsUsingIdsAndApplyViewDateResultsProjection(recordIds, eventsViewDateTimes));
	}

	public List<Event> fetchEvents(String eventType, int quantity) {

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		User currentUser = getCurrentUser();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);

		return rm.wrapEvents(searchServices.search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.eventSchema())
						.where(rm.eventType()).isEqualTo(eventType)
						.andWhere(rm.eventUsername()).isEqualTo(currentUser.getUsername()))
				.setNumberOfRows(quantity * 2)
				.sortDesc(rm.eventCreation())));

	}

	private List<String> getAllTaxonomyCodes() {
		List<String> allTaxonomyCodes = new ArrayList<String>();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		List<Taxonomy> collectionTaxonomies = taxonomiesManager.getAvailableTaxonomiesInHomePage(getCurrentUser());
		for (Taxonomy taxonomy : collectionTaxonomies) {
			String taxonomyCode = taxonomy.getCode();
			allTaxonomyCodes.add(taxonomyCode);
		}
		return allTaxonomyCodes;
	}

	private MetadataSchema getSchema(String schemaCode) {
		try {
			return schemaTypes != null ? schemaTypes.getDefaultSchema(schemaCode) : null;
		} catch (NoSuchSchemaType e) {
			return null;
		}
	}

	private MetadataSchema getFoldersSchema() {
		return getSchema(Folder.SCHEMA_TYPE);
	}

	private MetadataSchema getDocumentsSchema() {
		return getSchema(Document.SCHEMA_TYPE);
	}

	private Record getRecord(String id) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		return recordServices.getDocumentById(id);
	}

	public void forParams(String params) {
		String initialTabName = params;
		if (StringUtils.isBlank(initialTabName)) {
			String defaultTabUser = getCurrentUser().getStartTab();
			initialTabName = defaultTabUser;
		}
		RecordsManagementViewTab initialTab;
		if (StartTab.RECENT_FOLDERS.getCode().equals(initialTabName) || TAB_NAME_LAST_VIEWED_FOLDERS.equals(initialTabName)) {
			initialTab = tabLastViewedFolders;
		} else if (StartTab.RECENT_DOCUMENTS.getCode().equals(initialTabName) || TAB_NAME_LAST_VIEWED_DOCUMENTS
				.equals(initialTabName)) {
			initialTab = tabLastViewedDocuments;
		} else if (StartTab.TAXONOMIES.getCode().equals(initialTabName) || TAB_NAME_TAXONOMIES.equals(initialTabName)) {
			initialTab = tabTaxonomies;
		} else {
			initialTab = null;
		}
		if (initialTab == null || !initialTab.isEnabled()) {
			initialTab = tabs.get(0);
		}
		view.setTabs(tabs, initialTab);
	}

	public void addFolderButtonClicked() {
		view.navigateTo().addFolder(null, null);
	}

	public void addDocumentButtonClicked() {
		view.navigateTo().addDocument(null, null);
	}

	public void recordClicked(String id) {
		if (id != null && !id.startsWith("dummy")) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
			Record record = getRecord(id);
			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
			if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				view.navigateTo().displayFolder(id);
			} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
				view.navigateTo().displayDocument(id);
			} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
				view.navigateTo().displayContainer(id);
			}
		}
	}

	public User getCurrentUser() {
		return super.getCurrentUser();
	}

}
