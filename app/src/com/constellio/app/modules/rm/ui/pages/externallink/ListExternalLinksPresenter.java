package com.constellio.app.modules.rm.ui.pages.externallink;

import com.constellio.app.api.extensions.params.DocumentFolderBreadCrumbParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerPresenterParam;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb.DecommissionBreadcrumbTrail;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail.CurrentViewItem;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListExternalLinksPresenter extends BasePresenter<ListExternalLinksView> {

	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;
	private RecordServices recordServices;

	private String folderId;
	private List<ExternalLinkSource> sources;

	private Map<String, String> params = null;

	public ListExternalLinksPresenter(ListExternalLinksView view) {
		super(view);

		rm = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		sources = new ArrayList<>();
	}

	public void forParams(String params) {
		Map<String, String> lParamsAsMap = ParamUtils.getParamsMap(params);
		if (lParamsAsMap.size() > 0) {
			this.params = ParamUtils.getParamsMap(params);
			folderId = this.params.get("id");
		} else {
			folderId = params;
			this.params = new HashMap<>();
			this.params.put("id", folderId);
		}
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
				.andWhere(rm.externalLink.type()).isIn(types)).setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
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

	public BaseBreadcrumbTrail getBreadCrumbTrail() {
		final BaseBreadcrumbTrail baseBreadcrumbTrail;
		boolean forceBaseItemEnabled = true;
		String taxonomyCode = view.getUIContext().getAttribute(FolderDocumentContainerBreadcrumbTrail.TAXONOMY_CODE);
		String saveSearchDecommissioningId = null;
		String searchTypeAsString = null;
		String favoritesId = null;

		if (params != null) {
			if (params.get("decommissioningSearchId") != null) {
				saveSearchDecommissioningId = params.get("decommissioningSearchId");
				view.getUIContext()
						.setAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING, saveSearchDecommissioningId);
			}

			if (params.get("decommissioningType") != null) {
				searchTypeAsString = params.get("decommissioningType");
				view.getUIContext().setAttribute(DecommissioningBuilderViewImpl.DECOMMISSIONING_BUILDER_TYPE, searchTypeAsString);
			}
			favoritesId = params.get(RMViews.FAV_GROUP_ID_KEY);
		}

		SearchType searchType = null;
		if (searchTypeAsString != null) {
			searchType = SearchType.valueOf((searchTypeAsString));
		}
		BaseBreadcrumbTrail breadcrumbTrail;

		RMModuleExtensions rmModuleExtensions = view.getConstellioFactories().getAppLayerFactory().getExtensions()
				.forCollection(view.getCollection()).forModule(ConstellioRMModule.ID);
		breadcrumbTrail = rmModuleExtensions
				.getBreadCrumbtrail(new DocumentFolderBreadCrumbParams(folderId, params, view));

		if (breadcrumbTrail != null) {
			return breadcrumbTrail;
		} else if (favoritesId != null) {
			baseBreadcrumbTrail = new FolderDocumentContainerBreadcrumbTrail(new FolderDocumentContainerPresenterParam(folderId, null, null, favoritesId, view, forceBaseItemEnabled));
		} else if (saveSearchDecommissioningId == null) {
			String containerId = null;
			if (params != null && params instanceof Map) {
				containerId = params.get("containerId");
			}
			baseBreadcrumbTrail = new FolderDocumentContainerBreadcrumbTrail(new FolderDocumentContainerPresenterParam(folderId, taxonomyCode, containerId,
					null, view, forceBaseItemEnabled));
		} else {
			baseBreadcrumbTrail = new DecommissionBreadcrumbTrail($("DecommissioningBuilderView.viewTitle." + searchType.name()),
					searchType, saveSearchDecommissioningId, folderId, view, true);
		}
		BaseBreadcrumbTrail result = new BaseBreadcrumbTrail() {
			@Override
			protected void itemClick(BreadcrumbItem item) {
				baseBreadcrumbTrail.click(item);
			}
		};
		for (BreadcrumbItem breadcrumbItem : baseBreadcrumbTrail.getItems()) {
			result.addItem(breadcrumbItem);
		}
		result.addItem(new CurrentViewItem($("ListExternalLinksView.viewTitle")));
		return result;
	}

	public boolean hasExternalLinks() {
		return CollectionUtils.isEmpty(rm.getFolder(folderId).getExternalLinks()) ? false : true;
	}
}
