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
