package com.constellio.app.ui.pages.imports.settings;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.ui.pages.imports.ImportFileView;
import com.constellio.app.ui.pages.imports.ImportFileViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

public class ImportSettingsViewImpl extends ImportFileViewImpl implements ImportFileView {

	@Override
	protected void initPresenter() {
		presenter = new ImportSettingsPresenter(this);
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
		return $("ImportSettingsView.viewTitle");
	}

	@Override
	protected String getUploadFieldCaption() {
		return $("ImportSettingsView.configFile");
	}
	
}

