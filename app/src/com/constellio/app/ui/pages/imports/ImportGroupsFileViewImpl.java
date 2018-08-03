package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.framework.components.CollectionsSelectionPanel;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ImportGroupsFileViewImpl extends ImportFileViewImpl implements ImportFileView {

	private CollectionsSelectionPanel collectionsComponent;

	@Override
	protected void initPresenter() {
		presenter = new ImportGroupsFilePresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		super.buildMainComponent(event);
		List<String> collections = ((ImportGroupsFilePresenter) presenter).getAllCollections();
		String title = $("ImportGroupsFileViewImpl.collection");
		collectionsComponent = new CollectionsSelectionPanel(title, collections);
		mainLayout.addComponentAsFirst(collectionsComponent);
		return mainLayout;
	}

	@Override
	public List<String> getSelectedCollections() {
		return Arrays.asList(getCollection());
	}

	@Override
	protected String getTitle() {
		return $("ImportGroupsFileViewImpl.viewTitle");
	}
}

