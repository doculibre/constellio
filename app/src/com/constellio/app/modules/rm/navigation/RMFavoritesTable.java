package com.constellio.app.modules.rm.navigation;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.table.RecordVOSelectionTableAdapter;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class RMFavoritesTable implements Serializable {
	private transient AppLayerFactory appLayerFactory;
	private transient SessionContext sessionContext;
	private transient RMSchemasRecordsServices rm;
	private transient User user;

	public RMFavoritesTable(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		init(appLayerFactory, sessionContext);
	}

	public Component builtCustomSheet(ItemClickEvent.ItemClickListener itemClickListener) {
		List<RecordVODataProvider> providers = getDataProviders();
		TabSheet costumTabSheet = new TabSheet();
		for (RecordVODataProvider provider : providers) {
			switch (provider.getSchema().getTypeCode()) {
				case Folder.SCHEMA_TYPE:
					costumTabSheet.addTab(buildTable(provider, itemClickListener), $("HomeView.tab.customSheet.folders"));
					break;
				case Document.SCHEMA_TYPE:
					costumTabSheet.addTab(buildTable(provider, itemClickListener), $("HomeView.tab.customSheet.documents"));
					break;
				case ContainerRecord.SCHEMA_TYPE:
					costumTabSheet.addTab(buildTable(provider, itemClickListener), $("HomeView.tab.customSheet.containers"));
					break;
			}

		}
		return costumTabSheet;
	}

	private Component buildTable(RecordVODataProvider dataProvider,
								 ItemClickEvent.ItemClickListener itemClickListener) {
		RecordVOLazyContainer container = new RecordVOLazyContainer(dataProvider);
		final RecordVOTable table = new RecordVOTable(container);
		table.addStyleName("record-table");
		table.setSizeFull();
		for (Object item : table.getContainerPropertyIds()) {
			if (item instanceof MetadataVO) {
				MetadataVO property = (MetadataVO) item;
				if (property.getCode() != null && property.getCode().contains(Schemas.MODIFIED_ON.getLocalCode())) {
					table.setColumnWidth(property, 180);
				}
			}
		}
		table.addItemClickListener(itemClickListener);
		return new RecordVOSelectionTableAdapter(table) {
			@Override
			public void selectAll() {
				selectAllByItemId();
			}

			@Override
			public void deselectAll() {
				deselectAllByItemId();
			}

			@Override
			public boolean isAllItemsSelected() {
				return isAllItemsSelectedByItemId();
			}

			@Override
			public boolean isAllItemsDeselected() {
				return isAllItemsDeselectedByItemId();
			}

			@Override
			public boolean isSelected(Object itemId) {
				RecordVOItem item = (RecordVOItem) table.getItem(itemId);
				String recordId = item.getRecord().getId();
				return isRecordSelected(recordId);
			}

			@Override
			public void setSelected(Object itemId, boolean selected) {
				RecordVOItem item = (RecordVOItem) table.getItem(itemId);
				String recordId = item.getRecord().getId();
				selectionChanged(recordId, selected);
				adjustSelectAllButton(selected);
			}

			@Override
			protected boolean isIndexProperty() {
				return true;
			}
		};
	}

	private boolean isRecordSelected(String recordId) {
		return sessionContext.getSelectedRecordIds().contains(recordId);
	}

	void selectionChanged(String recordId, Boolean selected) {
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		Record record = searchServices
				.searchSingleResult(LogicalSearchQueryOperators.fromAllSchemasIn(sessionContext.getCurrentCollection())
						.where(Schemas.IDENTIFIER).isEqualTo(recordId));
		String schemaTypeCode = record == null ? null : record.getTypeCode();
		if (selected) {
			sessionContext.addSelectedRecordId(recordId, schemaTypeCode);
		} else {
			sessionContext.removeSelectedRecordId(recordId, schemaTypeCode);
		}
	}

	private List<RecordVODataProvider> getDataProviders() {
		final MetadataSchemaType folderSchemaType = rm.folderSchemaType();
		MetadataSchemaVO folderSchema = new MetadataSchemaToVOBuilder().build(folderSchemaType.getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		RecordVODataProvider folderVODataProvider = new RecordVODataProvider(folderSchema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(folderSchemaType).where(rm.folder.favorites()).isContaining(asList(user.getId())))
						.filteredByStatus(StatusFilter.ACTIVES).filteredWithUser(user);
			}
		};

		final MetadataSchemaType documentSchemaType = rm.documentSchemaType();
		MetadataSchemaVO documentSchema = new MetadataSchemaToVOBuilder().build(documentSchemaType.getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		RecordVODataProvider documentVODataProvider = new RecordVODataProvider(documentSchema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				final Metadata documentFavoritesList = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(sessionContext.getCurrentCollection()).getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FAVORITES);
				return new LogicalSearchQuery(from(documentSchemaType).where(documentFavoritesList).isContaining(asList(user.getId())))
						.filteredByStatus(StatusFilter.ACTIVES).filteredWithUser(user);
			}
		};

		final MetadataSchemaType containerSchemaType = rm.containerRecord.schemaType();
		MetadataSchemaVO containerSchema = new MetadataSchemaToVOBuilder().build(containerSchemaType.getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		RecordVODataProvider containerVODataProvider = new RecordVODataProvider(containerSchema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				final Metadata containerFavoritesList = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(sessionContext.getCurrentCollection()).getMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.FAVORITES);
				return new LogicalSearchQuery(from(containerSchemaType).where(containerFavoritesList).isContaining(asList(user.getId())))
						.filteredByStatus(StatusFilter.ACTIVES).filteredWithUser(user);
			}
		};

		return asList(folderVODataProvider, documentVODataProvider, containerVODataProvider);
	}

	private void init(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
		rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		user = new PresenterService(appLayerFactory.getModelLayerFactory()).getCurrentUser(sessionContext);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getAppLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}
}
