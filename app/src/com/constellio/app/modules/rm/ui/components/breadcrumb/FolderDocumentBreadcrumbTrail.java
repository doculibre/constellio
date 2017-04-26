package com.constellio.app.modules.rm.ui.components.breadcrumb;

import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrailPresenter.DocumentBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrailPresenter.FolderBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrailPresenter.TaxonomyBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrailPresenter.TaxonomyElementBreadcrumbItem;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.CollectionBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.SearchResultsBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.base.UIContextProvider;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;

public class FolderDocumentBreadcrumbTrail extends TitleBreadcrumbTrail implements UIContextProvider {
	
	private FolderDocumentBreadcrumbTrailPresenter presenter;

	public FolderDocumentBreadcrumbTrail(String recordId, String taxonomyCode, BaseView view) {
		super(view, null);
		this.presenter = new FolderDocumentBreadcrumbTrailPresenter(recordId, taxonomyCode, this);
	}

	@Override
	protected Button newButton(BreadcrumbItem item) {
		Button button = super.newButton(item);
		String recordId;
		if (item instanceof FolderBreadcrumbItem) {
			recordId = ((FolderBreadcrumbItem) item).getFolderId();
		} else if (item instanceof DocumentBreadcrumbItem) {
			recordId = ((DocumentBreadcrumbItem) item).getDocumentId();
		} else if (item instanceof TaxonomyElementBreadcrumbItem) {
			recordId = ((TaxonomyElementBreadcrumbItem) item).getTaxonomyElementId();
		} else if (item instanceof TaxonomyBreadcrumbItem) {
			recordId = null;
		} else if (item instanceof CollectionBreadcrumbItem) {
			recordId = null;
		} else if (item instanceof SearchResultsBreadcrumbItem) {
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
	public UIContext getUIContext() {
		return ConstellioUI.getCurrent();
	}

}
