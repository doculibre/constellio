package com.constellio.app.ui.pages.imports.authorization;

import com.constellio.app.ui.pages.imports.ImportFileView;
import com.constellio.app.ui.pages.imports.ImportFileViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ImportAuthorizationsFileViewImpl extends ImportFileViewImpl implements ImportFileView {

	@Override
	protected void initPresenter() {
		presenter = new ImportAuthorizationsFilePresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		super.buildMainComponent(event);
		return mainLayout;
	}

	@Override
	public List<String> getSelectedCollections() {
		return Arrays.asList(getCollection());
	}

	@Override
	protected String getTitle() {
		return $("ImportAuthorizationsFileViewImpl.viewTitle");
	}
}

