package com.constellio.app.modules.rm.ui.components.breadcrumb;

import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrailPresenter.DocumentBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrailPresenter.FolderBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;

public class FolderDocumentBreadcrumbTrail extends BaseBreadcrumbTrail {
	private FolderDocumentBreadcrumbTrailPresenter presenter;

	public FolderDocumentBreadcrumbTrail(String recordId) {
		this.presenter = new FolderDocumentBreadcrumbTrailPresenter(recordId, this);
	}

	@Override
	protected Button newButton(BreadcrumbItem item) {
		Button button = super.newButton(item);
		String recordId;
		if (item instanceof FolderBreadcrumbItem) {
			recordId = ((FolderBreadcrumbItem) item).getFolderId();
		} else if (item instanceof DocumentBreadcrumbItem) {
			recordId = ((DocumentBreadcrumbItem) item).getDocumentId();
		} else {
			throw new RuntimeException("Unrecognized breadcrumb item type : " + item.getClass());
		}
		Resource icon = FileIconUtils.getIconForRecordId(recordId);
		button.setIcon(icon);
		return button;
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		presenter.itemClicked(item);
	}

}
