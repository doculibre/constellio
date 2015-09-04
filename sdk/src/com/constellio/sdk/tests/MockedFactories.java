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
package com.constellio.sdk.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;

public class MockedFactories {

	ConstellioFactories constellioFactories;
	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	DataLayerFactory dataLayerFactory;
	AppLayerExtensions appExtensions;
	AppLayerCollectionExtensions appCollectionExtensions;
	AppLayerSystemExtensions appSystemExtensions;

	RecordServicesImpl recordServices;

	public MockedFactories() {
		constellioFactories = mock(ConstellioFactories.class, "constellioFactories");
		appLayerFactory = mock(AppLayerFactory.class, "appLayerFactory");
		modelLayerFactory = mock(ModelLayerFactory.class, "modelLayerFactory");
		dataLayerFactory = mock(DataLayerFactory.class, "dataLayerFactory");

		appExtensions = mock(AppLayerExtensions.class, "appExtensions");
		appCollectionExtensions = mock(AppLayerCollectionExtensions.class, "appCollectionExtensions");
		appSystemExtensions = mock(AppLayerSystemExtensions.class, "appSystemExtensions");

		when(constellioFactories.getAppLayerFactory()).thenReturn(appLayerFactory);
		when(constellioFactories.getModelLayerFactory()).thenReturn(modelLayerFactory);
		when(constellioFactories.getDataLayerFactory()).thenReturn(dataLayerFactory);

		when(appLayerFactory.getModelLayerFactory()).thenReturn(modelLayerFactory);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);

		when(appLayerFactory.getExtensions()).thenReturn(appExtensions);
		when(appExtensions.getSystemWideExtensions()).thenReturn(appSystemExtensions);
		when(appExtensions.forCollection("zeCollection")).thenReturn(appCollectionExtensions);
	}

	public ConstellioFactories getConstellioFactories() {
		return constellioFactories;
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public DataLayerFactory getDataLayerFactory() {
		return dataLayerFactory;
	}

	public RecordServices getRecordServices() {

		if (recordServices == null) {
			recordServices = mock(RecordServicesImpl.class, "recordServices");
			when(modelLayerFactory.newRecordServices()).thenReturn(recordServices);
		}

		return recordServices;
	}
}
