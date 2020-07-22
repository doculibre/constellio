package com.constellio.app.modules.rm.ui.components.document;

import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;

import java.io.Serializable;

/**
 * Implemented:
 * <p>
 * Document.TYPE
 *
 * @author Vincent
 */

public interface DocumentForm extends Serializable {

	void reload();

	void commit();

	ConstellioFactories getConstellioFactories();

	SessionContext getSessionContext();

	CustomDocumentField<?> getCustomField(String metadataCode);

	Layout getFieldLayout(Field<?> field);
}
