package com.constellio.app.ui.pages.management.reindexation;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ForcedReindexViewImpl extends BaseViewImpl implements ForcedReindexView {
	private final ForcedReindexPresenter presenter;

	TextArea hashes;

	public ForcedReindexViewImpl() {
		presenter = new ForcedReindexPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ForceReindexView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		hashes = new TextArea($("ForceReindexView.hashes"));
		hashes.setWidth("100%");

		Button reindex = new BaseButton($("ForceReindexView.reindex")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.reindex(hashes.getValue());
			}
		};
		reindex.addStyleName(ValoTheme.BUTTON_PRIMARY);

		VerticalLayout layout = new VerticalLayout(hashes, reindex);
		layout.setSpacing(true);
		layout.setWidth("100%");

		return layout;
	}

	@Override
	public void reindexFinished() {
		hashes.setValue("");
		showMessage($("ForceReindexView.reindex.finished"));
	}
}
