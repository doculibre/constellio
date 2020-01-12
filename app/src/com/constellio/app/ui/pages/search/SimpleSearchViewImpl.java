package com.constellio.app.ui.pages.search;

import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

public class SimpleSearchViewImpl extends SearchViewImpl<SimpleSearchPresenter> implements SimpleSearchView {

	public SimpleSearchViewImpl() {
		presenter = new SimpleSearchPresenter(this);
		presenter.resetFacetAndOrder();
	}

	@Override
	protected Component buildSearchUI() {
		setSearchExpression(presenter.getUserSearchExpression());
		return null;
	}

	@Override
	public Boolean computeStatistics() {
		return false;
	}

	@Override
	protected String getTitle() {
		return $("searchResults");
	}

	@Override
	protected boolean getExtraTab() {
		return true;
	}
}
