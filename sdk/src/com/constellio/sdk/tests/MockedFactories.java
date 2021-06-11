package com.constellio.sdk.tests;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockedFactories {

	ConstellioFactories constellioFactories;
	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	DataLayerFactory dataLayerFactory;
	AppLayerExtensions appExtensions;
	AppLayerCollectionExtensions appCollectionExtensions;
	AppLayerSystemExtensions appSystemExtensions;
	FoldersLocator foldersLocator;

	RecordServicesImpl recordServices;

	public MockedFactories() {
		constellioFactories = mock(ConstellioFactories.class, "constellioFactories");
		appLayerFactory = mock(AppLayerFactory.class, "appLayerFactory");
		modelLayerFactory = mock(ModelLayerFactory.class, "modelLayerFactory");
		dataLayerFactory = mock(DataLayerFactory.class, "dataLayerFactory");

		appExtensions = mock(AppLayerExtensions.class, "appExtensions");
		appCollectionExtensions = mock(AppLayerCollectionExtensions.class, "appCollectionExtensions");
		appSystemExtensions = mock(AppLayerSystemExtensions.class, "appSystemExtensions");
		foldersLocator = mock(FoldersLocator.class, "foldersLocator");

		when(constellioFactories.getAppLayerFactory()).thenReturn(appLayerFactory);
		when(constellioFactories.getModelLayerFactory()).thenReturn(modelLayerFactory);
		when(constellioFactories.getDataLayerFactory()).thenReturn(dataLayerFactory);

		when(appLayerFactory.getModelLayerFactory()).thenReturn(modelLayerFactory);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(modelLayerFactory.getFoldersLocator()).thenReturn(foldersLocator);

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
