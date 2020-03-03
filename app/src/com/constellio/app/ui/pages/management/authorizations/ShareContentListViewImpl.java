package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

public class ShareContentListViewImpl extends BaseViewImpl implements ShareContentListView {

	private ShareContentListPresenter presenter;

	public ShareContentListViewImpl() {
		presenter = new ShareContentListPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListContentShareView.viewTitle", presenter.getUserVO().getTitle());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return null;
	}


	public boolean isViewReadOnly() {
		return false;
	}
}
