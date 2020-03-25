package com.constellio.app.modules.rm.ui.pages.externallink;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListExternalLinksPresenter extends BasePresenter<ListExternalLinksView> {

	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;
	private RecordServices recordServices;

	private String folderId;
	private List<ExternalLinkSource> sources;

	public ListExternalLinksPresenter(ListExternalLinksView view) {
		super(view);

		rm = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		sources = new ArrayList<>();
	}

	public void forParams(String params) {
		folderId = params;
	}

	public void addSource(ExternalLinkSource source) {
		sources.add(source);
	}

	public List<ExternalLinkSource> getSources() {
		return sources;
	}

	public boolean hasSource() {
		return sources.size() > 0;
	}

	public boolean hasSingleSource() {
		return sources.size() == 1;
	}

	public String getFolderId() {
		return folderId;
	}

	public boolean hasResults(List<String> types) {
		return searchServices.hasResults(getQuery(types));
	}

	public RecordVODataProvider getDataProvider(List<String> types) {
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				rm.externalLink.schemaType().getDefaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());

		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(),
				view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return ListExternalLinksPresenter.this.getQuery(types);
			}
		};
	}

	private LogicalSearchQuery getQuery(List<String> types) {
		Folder folder = rm.getFolder(folderId);
		return new LogicalSearchQuery(from(rm.externalLink.schemaType())
				.where(Schemas.IDENTIFIER).isIn(folder.getExternalLinks())
				.andWhere(rm.externalLink.type()).isIn(types));
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Folder folder = rm.getFolder(folderId);
		folder.removeExternalLink(recordVO.getId());
		try {
			recordServices.update(folder.getWrappedRecord());
			recordServices.logicallyDelete(recordServices.getDocumentById(recordVO.getId()), getCurrentUser());
		} catch (RecordServicesException e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
		view.refreshTables();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}
}
