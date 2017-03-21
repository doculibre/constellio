package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

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

	@Override
	protected String getTitle() {
		return $("searchResults");
	}
	
}
