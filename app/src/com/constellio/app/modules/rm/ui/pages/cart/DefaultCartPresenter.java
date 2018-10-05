package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingPresenterService;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class DefaultCartPresenter extends CartPresenter {
	private transient RMSchemasRecordsServices rm;
	private transient Cart cart;
	private String batchProcessSchemaType;

	private transient BatchProcessingPresenterService batchProcessingPresenterService;
	private transient ModelLayerCollectionExtensions modelLayerExtensions;
	private transient RMModuleExtensions rmModuleExtensions;

	public DefaultCartPresenter(DefaultCartView view) {
		super(view);
		modelLayerExtensions = modelLayerFactory.getExtensions().forCollection(view.getCollection());
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(view.getCollection()).forModule(ConstellioRMModule.ID);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public RecordVOWithDistinctSchemasDataProvider getFolderRecords() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.FAVORITES_LIST);
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rm.folder.schemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
				return logicalSearchQuery;
			}
		};
	}

	public RecordVOWithDistinctSchemasDataProvider getDocumentRecords() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FAVORITES_LIST);
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.documentSchemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVOWithDistinctSchemasDataProvider getContainerRecords() {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.FAVORITES_LIST);
		return new RecordVOWithDistinctSchemasDataProvider(
				getSchemas(), new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.documentSchemaType()).where(metadata).isContaining(asList(getCurrentUser().getId())))
						.filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES)
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public void displayRecordRequested(RecordVO recordVO) {
	}

	public void itemRemovalRequested(RecordVO recordVO) {
	}
}
