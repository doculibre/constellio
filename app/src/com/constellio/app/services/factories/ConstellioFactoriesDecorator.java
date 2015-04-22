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
package com.constellio.app.services.factories;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConstellioFactoriesDecorator {

	public DataLayerConfiguration decorateDataLayerConfiguration(DataLayerConfiguration dataLayerConfiguration) {
		return dataLayerConfiguration;
	}

	public ModelLayerConfiguration decorateModelLayerConfiguration(ModelLayerConfiguration modelLayerConfiguration) {
		return modelLayerConfiguration;
	}

	public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {
		return appLayerConfiguration;
	}

	public DataLayerFactory decorateDataLayerFactory(DataLayerFactory dataLayerFactory) {
		return dataLayerFactory;
	}

	public ModelLayerFactory decorateModelServicesFactory(ModelLayerFactory modelLayerFactory) {
		return modelLayerFactory;
	}

	public AppLayerFactory decorateAppServicesFactory(AppLayerFactory appLayerFactory) {
		return appLayerFactory;
	}

	public FoldersLocator decorateFoldersLocator(FoldersLocator foldersLocator) {
		return foldersLocator;
	}

	public StatefullServiceDecorator getStatefullServiceDecorator() {
		return new StatefullServiceDecorator();
	}
}
