package com.constellio.app.modules.rm.ui.components.breadcrumb;

import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter.ContainerBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter.TaxonomyBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter.TaxonomyElementBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter.TriggerFormBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter.TriggerManagerBreadcrumbItem;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.CollectionBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.FavoritesBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.GroupFavoritesBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.LastViewedFoldersDocumentsBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.ListContentAccessAndRoleAuthorizationsBreadCrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.SearchResultsBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.base.UIContextProvider;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.GetRecordOptions;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;

public class FolderDocumentContainerBreadcrumbTrail extends TitleBreadcrumbTrail implements UIContextProvider {

	private FolderDocumentContainerBreadcrumbTrailPresenter presenter;

	public FolderDocumentContainerBreadcrumbTrail(FolderDocumentContainerPresenterParam presenterParam) {
		super(presenterParam.getView(), null, false);
		this.presenter = getPresenter(presenterParam);
	}

	protected FolderDocumentContainerBreadcrumbTrailPresenter getPresenter(
			FolderDocumentContainerPresenterParam folderDocumentContainerPresenterParam) {
		return new FolderDocumentContainerBreadcrumbTrailPresenter(folderDocumentContainerPresenterParam.getRecordId(), folderDocumentContainerPresenterParam.getTaxonomyCode(),
				this, folderDocumentContainerPresenterParam.getContainerId(), folderDocumentContainerPresenterParam.getFavoritesId(), folderDocumentContainerPresenterParam.isForceBaseItemEnabled());
	}

	@Override
	protected Button newButton(BreadcrumbItem item) {
		Button button = super.newButton(item);
		Record record;
		if (item instanceof FolderBreadCrumbItem) {
			record = getSummaryRecord(((FolderBreadCrumbItem) item).getFolderId());
		} else if (item instanceof DocumentBreadCrumbItem) {
			record = getSummaryRecord(((DocumentBreadCrumbItem) item).getDocumentId());
		} else if (item instanceof TaxonomyElementBreadcrumbItem) {
			record = getRecord(((TaxonomyElementBreadcrumbItem) item).getTaxonomyElementId());
		} else if (item instanceof TaxonomyBreadcrumbItem) {
			record = null;
		} else if (item instanceof CollectionBreadcrumbItem) {
			record = null;
		} else if (item instanceof SearchResultsBreadcrumbItem) {
			record = null;
		} else if(item instanceof ContainerBreadcrumbItem) {
			record = getRecord(((ContainerBreadcrumbItem) item).getContainerId());
		} else if (item instanceof ViewGroupBreadcrumbItem) {
			record = null;
		} else if (item instanceof IntermediateBreadCrumbTailItem) {
			record = null;
		} else if (item instanceof GroupFavoritesBreadcrumbItem) {
			record = getRecord(((GroupFavoritesBreadcrumbItem) item).getFavoriteGroupId());
		} else if (item instanceof FavoritesBreadcrumbItem) {
			record = null;
		} else if (item instanceof LastViewedFoldersDocumentsBreadcrumbItem) {
			record = null;
		} else if (item instanceof ListContentAccessAndRoleAuthorizationsBreadCrumbItem) {
			record = null;
		} else if (item instanceof TriggerManagerBreadcrumbItem) {
			record = null;
		} else if (item instanceof TriggerFormBreadcrumbItem) {
			record = null;
		} else {
			throw new RuntimeException("Unrecognized breadcrumb item type : " + item.getClass());
		}
		if (record != null) {
			Resource icon = FileIconUtils.getIconForRecordId(record, false);
			button.setIcon(icon);
		}
		return button;
	}

	private Record getRecord(String recordId) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		return appLayerFactory.getModelLayerFactory().newRecordServices().get(recordId);
	}

	private Record getSummaryRecord(String recordId) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		return appLayerFactory.getModelLayerFactory().newRecordServices().get(recordId, GetRecordOptions.RETURNING_SUMMARY);
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		if (!presenter.itemClicked(item)) {
			super.itemClick(item);
		}
	}

	@Override
	public UIContext getUIContext() {
		return ConstellioUI.getCurrent();
	}

}
