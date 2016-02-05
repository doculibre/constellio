package com.constellio.app.ui.pages.management.facet.fields;

import com.constellio.app.modules.rm.ui.components.facet.FacetFieldFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;

public abstract class FacetConfigurationFormImpl extends RecordForm implements FacetConfigurationForm {

	public FacetConfigurationFormImpl(RecordVO record, FacetFieldFactory facetFieldFactory) {
		super(record, facetFieldFactory);
	}

	@Override
	public ValuesLabelField getCustomField(String metadataCode) {
		return (ValuesLabelField) getField(metadataCode);
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

}
