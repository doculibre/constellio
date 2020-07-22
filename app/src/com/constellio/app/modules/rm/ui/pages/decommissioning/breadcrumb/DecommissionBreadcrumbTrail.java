package com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb;

import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.components.breadcrumb.DocumentBreadCrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderBreadCrumbItem;
import com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb.DecommissionBreadcrumbTrailPresenter.ArchiveManagementBreadCrumbItem;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.CollectionBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.SearchResultsBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.base.UIContextProvider;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;

import java.util.List;

public class DecommissionBreadcrumbTrail extends TitleBreadcrumbTrail implements UIContextProvider {

	private DecommissionBreadcrumbTrailPresenter presenter;

	public DecommissionBreadcrumbTrail(String title, SearchType type, String searchId, String recordId, BaseView view,
									   boolean forceBaseItemEnabled) {
		super(view, null);
		this.presenter = new DecommissionBreadcrumbTrailPresenter(title, type, searchId, recordId, this, forceBaseItemEnabled);
	}

	@Override
	protected Button newButton(BreadcrumbItem item) {
		Button button = super.newButton(item);
		String recordId;
		if (item instanceof CollectionBreadcrumbItem) {
			recordId = null;
		} else if (item instanceof ArchiveManagementBreadCrumbItem) {
			recordId = null;
		} else if (item instanceof FolderBreadCrumbItem) {
			recordId = ((FolderBreadCrumbItem) item).getFolderId();
		} else if (item instanceof DocumentBreadCrumbItem) {
			recordId = ((DocumentBreadCrumbItem) item).getDocumentId();
		} else if (item instanceof SearchResultsBreadcrumbItem) {
			recordId = null;
		} else if (item instanceof ViewGroupBreadcrumbItem) {
			recordId = null;
		} else if (item instanceof DecommissionBreadcrumbTrailPresenter.DispositionListItemBreadcrumbItem) {
			recordId = null;
		} else if (item instanceof DecommissionBreadcrumbTrailPresenter.DispositionListBreadcrumbItem) {
			recordId = null;
		} else {
			throw new RuntimeException("Unrecognized breadcrumb item type : " + item.getClass());
		}
		if (recordId != null) {
			Resource icon = FileIconUtils.getIconForRecordId(recordId);
			button.setIcon(icon);
		}
		return button;
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		if (!presenter.itemClicked(item)) {
			super.itemClick(item);
		}
	}

	@Override
	public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
		return super.getIntermediateItems();
	}

	@Override
	public UIContext getUIContext() {
		return ConstellioUI.getCurrent();
	}

}
