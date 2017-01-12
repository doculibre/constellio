package com.constellio.app.ui.framework.components.breadcrumb.taxonomy;

import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;

public class TaxonomyBreadcrumbTrail extends BaseBreadcrumbTrail {
	
	private TaxonomyBreadcrumbTrailPresenter presenter;

	public TaxonomyBreadcrumbTrail(String taxonomyCode, String recordId) {
		this.presenter = new TaxonomyBreadcrumbTrailPresenter(taxonomyCode,recordId, this);
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		presenter.itemClicked(item);
	}
}
