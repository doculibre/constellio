package com.constellio.app.modules.tasks.ui.components.fields;

import java.io.Serializable;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

/**
 * Implemented:
 *
 * Task.PROGRESS_PERCENTAGE
 *
 * @author Vincent
 */
public interface TaskForm extends Serializable {

	ConstellioFactories getConstellioFactories();

	SessionContext getSessionContext();

	CustomTaskField<?> getCustomField(String metadataCode);

	void reload();

	void commit();

}
