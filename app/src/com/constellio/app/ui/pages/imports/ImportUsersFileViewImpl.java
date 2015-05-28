/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
