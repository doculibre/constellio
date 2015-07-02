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

import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.app.services.schemas.bulkImport.UserImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;

import java.io.File;
import java.util.List;

public class ImportUsersFilePresenter extends ImportFilePresenter{
    public ImportUsersFilePresenter(ImportFileView view) {
        super(view);
        FoldersLocator foldersLocator = new FoldersLocator();
        File resourcesFolder = foldersLocator.getResourcesFolder();
        File exampleExcelFile = new File(resourcesFolder, "UserImportServices-user.xml");
        view.setExampleFile(exampleExcelFile);
    }

    @Override
    protected ImportServices newImportServices(ModelLayerFactory modelLayerFactory) {
        return new UserImportServices(modelLayerFactory);
    }

    public List<String> getAllCollections() {
        return appLayerFactory.getCollectionsManager().getCollectionCodes();
    }
}
