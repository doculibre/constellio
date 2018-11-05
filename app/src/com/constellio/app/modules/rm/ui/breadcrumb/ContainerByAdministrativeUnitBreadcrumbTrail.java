package com.constellio.app.modules.rm.ui.breadcrumb;

import com.constellio.app.modules.rm.ui.breadcrumb.ContainerByAdminsitrativeUnitBreadcrumbTrailPresenter.AdministrativeUnitBreadcrumbItem;
import com.constellio.app.modules.rm.ui.breadcrumb.ContainerByAdminsitrativeUnitBreadcrumbTrailPresenter.RecordCurrentViewItem;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.CollectionBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.base.UIContextProvider;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;

public class ContainerByAdministrativeUnitBreadcrumbTrail extends TitleBreadcrumbTrail implements UIContextProvider {

	private ContainerByAdminsitrativeUnitBreadcrumbTrailPresenter presenter;


	public ContainerByAdministrativeUnitBreadcrumbTrail(String recordId, String fromAdministrativeUnit,  BaseView view, String tabName) {
		super(view, null, true);
		this.presenter = new ContainerByAdminsitrativeUnitBreadcrumbTrailPresenter(recordId, fromAdministrativeUnit, this, tabName);
	}

	@Override
	protected Button newButton(BreadcrumbItem item) {
		Button button = super.newButton(item);
		String recordId;
		if (item instanceof AdministrativeUnitBreadcrumbItem) {
			recordId = ((AdministrativeUnitBreadcrumbItem) item).getId();
		} else if (item instanceof CollectionBreadcrumbItem) {
			recordId = null;
		} else if (item instanceof IntermediateBreadCrumbTailItem) {
			recordId = null;
		} else if (item instanceof ViewGroupBreadcrumbItem) {
			recordId = null;
		} else if (item instanceof RecordCurrentViewItem) {
			recordId = ((RecordCurrentViewItem) item).getRecordId();
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
