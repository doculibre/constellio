package com.constellio.app.ui.pages.search;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class SimpleSearchViewImpl extends SearchViewImpl<SimpleSearchPresenter> implements SimpleSearchView {

	public SimpleSearchViewImpl() {
		presenter = new SimpleSearchPresenter(this);
		presenter.resetFacetAndOrder();
	}

	@Override
	protected Component buildSearchUI() {
		setSearchExpression(presenter.getUserSearchExpression());
		return new VerticalLayout();
	}

	@Override
	public Boolean computeStatistics() {
		return false;
	}
}
