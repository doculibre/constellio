package com.constellio.app.modules.rm.ui.components.document;

import java.io.Serializable;

import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

/**
 * Implemented:
 *
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

}
