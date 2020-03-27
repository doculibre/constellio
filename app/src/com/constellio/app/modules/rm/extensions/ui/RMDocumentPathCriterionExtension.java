package com.constellio.app.modules.rm.extensions.ui;

import com.constellio.app.api.extensions.SearchCriterionExtension;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.fields.lookup.PathLookupField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Component;

public class RMDocumentPathCriterionExtension extends SearchCriterionExtension {

	AppLayerFactory appLayerFactory;
	String collection;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	RecordServices recordServices;

	public RMDocumentPathCriterionExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	@Override
	public Component getComponentForCriterion(Criterion criterion) {
		Component component = null;

		if (criterion.getSchemaType().equals(Document.SCHEMA_TYPE)
			&& criterion.getMetadataCode().endsWith(Schemas.PATH.getCode())) {
			component = buildComponentForDocumentPath(criterion);
		}

		return component;
	}

	private Component buildComponentForDocumentPath(final Criterion criterion) {
		final PathLookupField lookup = new PathLookupField(Folder.SCHEMA_TYPE);
		lookup.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX);
		lookup.setValue((String) criterion.getValue());

		lookup.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				criterion.setValue(lookup.getValue());
			}
		});

		I18NHorizontalLayout component = new I18NHorizontalLayout(lookup);
		component.setExpandRatio(lookup, 1);
		component.setWidth("100%");
		component.setSpacing(true);

		return component;
	}
}
