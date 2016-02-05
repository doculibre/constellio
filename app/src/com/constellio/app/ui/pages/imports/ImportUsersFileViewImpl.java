package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.framework.components.CollectionsSelectionPanel;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ImportUsersFileViewImpl extends ImportFileViewImpl implements ImportFileView{
    private CollectionsSelectionPanel collectionsComponent;

    @Override
    protected void initPresenter() {
        presenter = new ImportUsersFilePresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        super.buildMainComponent(event);
        List<String> collections = ((ImportUsersFilePresenter)presenter).getAllCollections();
        String title = $("ImportUsersFileViewImpl.collection");
        collectionsComponent = new CollectionsSelectionPanel(title, collections);
        mainLayout.addComponentAsFirst(collectionsComponent);
        return mainLayout;
    }

    @Override
    public List<String> getSelectedCollections() {
        return collectionsComponent.getSelectedCollections();
    }

    @Override
    protected String getTitle() {
        return $("ImportUsersFileViewImpl.viewTitle");
    }
}
