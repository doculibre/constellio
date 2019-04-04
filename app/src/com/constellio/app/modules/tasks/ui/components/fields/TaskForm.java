package com.constellio.app.modules.tasks.ui.components.fields;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Field;

/**
 * Implemented:
 * <p>
 * Task.PROGRESS_PERCENTAGE
 *
 * @author Vincent
 */
public interface TaskForm extends Serializable {

	ConstellioFactories getConstellioFactories();

	SessionContext getSessionContext();

	CustomTaskField<?> getCustomField(String metadataCode);

	Field<?> getField(String metadataCode);

	List<Field<?>> getFields();

	void reload();

	void commit();

}
