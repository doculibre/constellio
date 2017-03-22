package com.constellio.app.ui.framework.components.breadcrumb.taxonomy;

import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;

public class TaxonomyBreadcrumbTrail extends TitleBreadcrumbTrail {
	
	private TaxonomyBreadcrumbTrailPresenter presenter;

	public TaxonomyBreadcrumbTrail(String taxonomyCode, String recordId, BaseView view, String viewTitle) {
		super(view, viewTitle);
		this.presenter = new TaxonomyBreadcrumbTrailPresenter(taxonomyCode,recordId, this);
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		if (!presenter.itemClicked(item)) {
			super.itemClick(item);
		}
	}
}
