package com.constellio.app.ui.pages.management.facet.fields;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

import java.io.Serializable;

/**
 * Implemented:
 * <p>
 * Task.PROGRESS_PERCENTAGE
 *
 * @author Vincent
 */
public interface FacetConfigurationForm extends Serializable {

	void reload();

	void commit();

	ConstellioFactories getConstellioFactories();

	SessionContext getSessionContext();

	ValuesLabelField getCustomField(String metadataCode);

}
